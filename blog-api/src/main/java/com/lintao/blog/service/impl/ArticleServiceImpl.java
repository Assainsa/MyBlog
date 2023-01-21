package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lintao.blog.dao.dos.Archives;
import com.lintao.blog.dao.mapper.ArticleBodyMapper;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.mapper.ArticleTagMapper;
import com.lintao.blog.dao.pojo.Article;
import com.lintao.blog.dao.pojo.ArticleBody;
import com.lintao.blog.dao.pojo.ArticleTag;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.*;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ArticleBodyVo;
import com.lintao.blog.vo.ArticleVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.TagVo;
import com.lintao.blog.vo.params.ArticleParam;
import com.lintao.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ThreadService threadService;

    /**
     * 分页查询article数据库表，得到结果
     * @param pageParams
     * @return
     */
    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> articlePage = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //先是否置顶排序,然后创建日期排序
        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
        Page<Article> articlePage1 = articleMapper.selectPage(articlePage, queryWrapper);
        List<Article> records = articlePage1.getRecords();
        List<ArticleVo> articleVos = copyList(records,true,true);
        return Result.success(articleVos);
    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // select id, title from article order by view_counts desc limit 5
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // select id, title from article order by create_date desc limit 5
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    /**
     * 1. 根据id查询文章信息
     * 2. 根据bodyId和categoryId做关联查询
     * @param articleId
     * @return
     */
    @Override
    public Result findArticleById(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true,true,true);
        //把更新阅读次数放入线程池中进行操作，与主线程隔离
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    /**
     * 发布文章
     * 1. 构建Article对象
     * 2. 作者id 当前的登录用户中获取
     * 3. 标签 将标签加入到关联列表中
     * 4. body 内容存储 article bodyId
     * @param articleParam
     * @return
     */
    @Override
    public Result publish(ArticleParam articleParam) {
        //用户信息要从ThreadLocal中获取，因此此接口要加入到登录拦截器中
        SysUser sysUser = UserThreadLocal.get();
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(articleParam.getCategory().getId());
        //插入之后会生成一个文章id
        articleMapper.insert(article);
        Long articleId = article.getId();
        //把文章关联的所有标签都加入到关联表中
        List<TagVo> tags = articleParam.getTags();
        if (tags!=null){
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
        map.put("id",article.getId().toString());
        return Result.success(map);
    }

    /**
     * 将article列表转化为articleVo列表
     * @param records
     * @return
     */
    private List<ArticleVo> copyList(List<Article> records,boolean isTag, boolean isAuthor){
        List<ArticleVo> articleVos = new ArrayList<>();
        for (Article record:records){
            articleVos.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVos;
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory){
        List<ArticleVo> articleVos = new ArrayList<>();
        for (Article record:records){
            articleVos.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVos;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article,articleVo);
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-mm-dd HH:mm"));
        if (isTag){
            articleVo.setTags(tagService.findTagsByArticleId(article.getId()));
        }
        if (isAuthor){
            articleVo.setAuthor(sysUserService.findUserById(article.getAuthorId()).getNickname());
        }

        if (isBody){
            articleVo.setBody(findArticleBodyById(article.getBodyId()));
        }
        if (isCategory){
            articleVo.setCategory(categoryService.findCategoryById(article.getCategoryId()));
        }
        return articleVo;
    }

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
