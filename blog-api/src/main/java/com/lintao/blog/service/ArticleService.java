package com.lintao.blog.service;

import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.ArticleParam;
import com.lintao.blog.vo.params.PageParams;
import com.lintao.blog.vo.params.SearchParam;

public interface ArticleService {

    /**
     * 分页查询 文章列表
     * @param pageParams
     * @return
     */
    Result listArticle(PageParams pageParams);

    /**
     * 最热文章
     * @param limit
     * @return
     */
    Result hotArticle(int limit);

    /**
     * 最新文章
     * @param limit
     * @return
     */
    Result newArticles(int limit);

    /**
     * 文章归档
     * @return
     */
    Result listArchives();

    /**
     * 查看文章详情
     * @param articleId
     * @return
     */
    Result findArticleById(Long articleId);

    Result publish(ArticleParam articleParam);

    Result searchArticle(SearchParam searchParam);

    Result deleteArticleById(Long articleId);

    Result findArticleByAuthorId(Long authorId);

    Result processArticleById(Long articleId, boolean view);

    void replaceUrl();

    /*    Result updateTime();*/
}
