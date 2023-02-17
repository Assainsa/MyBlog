package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lintao.blog.common.cache.Cache;
import com.lintao.blog.dao.dos.Archives;
import com.lintao.blog.dao.mapper.ArticleBodyMapper;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.mapper.ArticleTagMapper;
import com.lintao.blog.dao.pojo.*;
import com.lintao.blog.service.*;
import com.lintao.blog.utils.QiniuUtils;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.*;
import com.lintao.blog.vo.params.ArticleParam;
import com.lintao.blog.vo.params.PageParams;
import com.lintao.blog.vo.params.SearchParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;


    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 分页查询article数据库表，得到结果
     *
     * @param pageParams
     * @return
     */
    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> articlePage = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<Article> articleIPage = articleMapper.listArticle(articlePage, pageParams.getCategoryId(), pageParams.getTagId(), pageParams.getYear(), pageParams.getMonth(), pageParams.getAuthorId());
        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records, true, true));

        /*Page<Article> articlePage = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (pageParams.getCategoryId()!=null){
            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
        }
        ArrayList<Long> articleIdList = new ArrayList<>();
        if (pageParams.getTagId()!=null){
            //从关联表article-tag中查出特定tag的文章列表
            LambdaQueryWrapper<ArticleTag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            tagLambdaQueryWrapper.eq(ArticleTag::getId,pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagLambdaQueryWrapper);
            if (articleTags.size()>0) {
                for (ArticleTag tag : articleTags) {
                    articleIdList.add(tag.getArticleId());
                }
            }
            queryWrapper.in(Article::getId,articleIdList);
        }
        //先是否置顶排序,然后创建日期排序
        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
        Page<Article> articlePage1 = articleMapper.selectPage(articlePage, queryWrapper);
        List<Article> records = articlePage1.getRecords();
        List<ArticleVo> articleVos = copyList(records,true,true);
        return Result.success(articleVos);*/
    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // select id, title from article order by view_counts desc limit 5
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // select id, title from article order by create_date desc limit 5
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    /**
     * 1. 根据id查询文章信息
     * 2. 根据bodyId和categoryId做关联查询
     *
     * @param articleId
     * @return
     */
    @Cache(expire = 5 * 60 * 1000, name = "view_Article")
    @Override
    public Result findArticleById(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true, true, true);
        return Result.success(articleVo);
    }

    /**
     * 发布文章
     * 1. 构建Article对象
     * 2. 作者id 当前的登录用户中获取
     * 3. 标签 将标签加入到关联列表中
     * 4. body 内容存储 article bodyId
     *
     * @param articleParam
     * @return
     */
    @Override
    public Result publish(ArticleParam articleParam) {
        //用户信息要从ThreadLocal中获取，因此此接口要加入到登录拦截器中
        SysUser sysUser = UserThreadLocal.get();
        Article article = new Article();
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCategoryId(articleParam.getCategory().getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        article.setCreateDate(Long.valueOf(dateFormat.format(System.currentTimeMillis())));
        List<TagVo> tags = articleParam.getTags();
        Long articleId;
        boolean isEdit = false;
        if (articleParam.getId() != null) isEdit = true;
        if (isEdit) {
            article.setId(articleParam.getId());
            //删掉原来存在的tag
            LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ArticleTag::getArticleId, article.getId());
            articleTagMapper.delete(queryWrapper);
            //删掉原来存在的body
            LambdaQueryWrapper<ArticleBody> queryWrapperBody = new LambdaQueryWrapper<>();
            queryWrapperBody.eq(ArticleBody::getArticleId, article.getId());
            articleBodyMapper.delete(queryWrapperBody);
        } else {
            article.setAuthorId(sysUser.getId());
            article.setWeight(Article.Article_Common);
            article.setViewCounts(0);
            article.setCommentCounts(0);
            //插入之后会生成一个文章id
            articleMapper.insert(article);
        }
        articleId = article.getId();
        //把文章关联的所有标签都加入到关联表中
        if (tags != null) {
            for (TagVo tag : tags) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(tag.getId());
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        //把文章内容加入到关联表中
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(articleId);
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        //插入之后才会产生articleBodyId，更新以后再更新article
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());
        //发一条信息给rocketmq，当前文章更新/发布了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(article.getId());
        rocketMQTemplate.convertAndSend("blog-update-article", articleMessage);
        return Result.success(map);
    }

    @Override
    public Result searchArticle(SearchParam searchParam) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.like(Article::getTitle, searchParam.getSearch());
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    @Override
    public Result deleteArticleById(Long articleId) {
        articleMapper.deleteById(articleId);
        LambdaQueryWrapper<ArticleBody> bodyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        bodyLambdaQueryWrapper.eq(ArticleBody::getArticleId, articleId);
        //丢给线程池，在对象存储中删除文章里包含的图片文件
        ArticleBody articleBody = articleBodyMapper.selectOne(bodyLambdaQueryWrapper);
        threadService.deleteImages(articleBody);
        //删除articleBody
        articleBodyMapper.delete(bodyLambdaQueryWrapper);
        //删除articleTag
        LambdaQueryWrapper<ArticleTag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tagLambdaQueryWrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(tagLambdaQueryWrapper);
        //删除comment
        Result deleteResult = commentService.deleteCommentByArticleId(articleId);
        //发一条信息给rocketmq，当前文章更新了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        rocketMQTemplate.convertAndSend("blog-update-article", articleMessage);
        return deleteResult;
    }

    @Override
    public Result findArticleByAuthorId(Long authorId) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getAuthorId, authorId);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, true, true, true, true));
    }

    @Override
    public Result processArticleById(Long articleId, boolean view) {
        //把更新阅读次数放入线程池中进行操作，与主线程隔离
        if (view) {
            threadService.updateArticleViewCount(articleId);
        }
        return findArticleById(articleId);
    }

    /*@Override
    public Result updateTime() {
        List<Article> UpdateArticles = new ArrayList<>();
        List<Article> articles = articleMapper.selectList(null);
        for (Article article:articles){
            Article update = new Article();
            update.setId(update.getId());
            Long createDate = article.getCreateDate();
            update.setCreateDate(new SimpleDateFormat("yyyyMMddHHmmss").format(createDate));
        }
    }*/

    /**
     * 将article列表转化为articleVo列表
     *
     * @param records
     * @return
     */
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVos = new ArrayList<>();
        for (Article record : records) {
            articleVos.add(copy(record, isTag, isAuthor, false, false));
        }
        return articleVos;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        List<ArticleVo> articleVos = new ArrayList<>();
        for (Article record : records) {
            articleVos.add(copy(record, isTag, isAuthor, isBody, isCategory));
        }
        return articleVos;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        try {
            BeanUtils.copyProperties(article, articleVo);
            if (article.getCreateDate() != null) {
                Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(String.valueOf(article.getCreateDate()));
                articleVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            }
            if (isTag) {
                articleVo.setTags(tagService.findTagsByArticleId(article.getId()));
            }
            if (isAuthor) {
                SysUser author = sysUserService.findUserById(article.getAuthorId());
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(author, userVo);
                articleVo.setAuthor(userVo);
            }

            if (isBody) {
                articleVo.setBody(findArticleBodyById(article.getBodyId()));
            }
            if (isCategory) {
                articleVo.setCategory(categoryService.findCategoryById(article.getCategoryId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleVo;
    }

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }

    public void replaceUrl() {
        List<ArticleBody> articleBodies = articleBodyMapper.selectList(new LambdaQueryWrapper<>());
        for (ArticleBody articleBody : articleBodies) {
            String content = articleBody.getContent();
            String contentHtml = articleBody.getContentHtml();
            String replaceContent = StringUtils.replace(content, "rovo5kjl1.hn-bkt.clouddn.com", "qny.tzsblog.xyz");
            String replaceContentHtml = StringUtils.replace(contentHtml, "rovo5kjl1.hn-bkt.clouddn.com", "qny.tzsblog.xyz");
            articleBody.setContent(replaceContent);
            articleBody.setContentHtml(replaceContentHtml);
            articleBodyMapper.updateById(articleBody);
        }
    }
}
