package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.pojo.Article;
import com.lintao.blog.service.ArticleService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.ArticleVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;

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

    /**
     * 将article列表转化为articleVo列表
     * @param records
     * @return
     */
    private List<ArticleVo> copyList(List<Article> records,boolean isTag, boolean isAuthor){
        List<ArticleVo> articleVos = new ArrayList<>();
        for (Article record:records){
            articleVos.add(copy(record,isTag,isAuthor));
        }
        return articleVos;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article,articleVo);
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-mm-dd HH:mm"));
        if (isTag){
            articleVo.setTags(tagService.findTagsByArticleId(article.getId()));
        }
        if (isAuthor){
            articleVo.setAuthor(sysUserService.findUserById(article.getAuthorId()).getNickname());
        }
        return articleVo;
    }
}
