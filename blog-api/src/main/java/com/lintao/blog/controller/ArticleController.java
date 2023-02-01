package com.lintao.blog.controller;

import com.lintao.blog.common.aop.LogAnnotation;
import com.lintao.blog.common.cache.Cache;
import com.lintao.blog.service.ArticleService;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.ArticleParam;
import com.lintao.blog.vo.params.PageParams;
import com.lintao.blog.vo.params.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController //json数据进行交互
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @LogAnnotation(module = "文章", operator = "获取文章列表")
    /*@Cache(expire = 5*60*1000,name = "list_article")*/
    @PostMapping
    public Result listArticle(@RequestBody PageParams pageParams){
        return articleService.listArticle(pageParams);
    }

    /**
     * 首页 最热文章
     * @param pageParams
     * @return
     */
    @LogAnnotation(module = "文章", operator = "获取最热文章")
    /*@Cache(expire = 5*60*1000,name = "hot_article")*/
    @PostMapping("hot")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 首页 最新文章
     * @param pageParams
     * @return
     */
    @LogAnnotation(module = "文章", operator = "获取最新文章")
    /*@Cache(expire = 5*60*1000,name = "new_article")*/
    @PostMapping("new")
    public Result newArticles(){
        int limit = 5;
        return articleService.newArticles(limit);
    }

    /**
     * 文章归档
     * @return
     */
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId, true);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

    @PostMapping("{id}")
    public Result getArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId,false);
    }

    @PostMapping("search")
    public Result searchArticle(@RequestBody SearchParam searchParam){
        return articleService.searchArticle(searchParam);
    }

    @PostMapping("delete/{id}")
    public Result deleteArticleById(@PathVariable("id") Long articleId){
        return articleService.deleteArticleById(articleId);
    }

    @PostMapping("author/{id}")
    public Result getArticleByAuthorId(@PathVariable("id") Long authorId){
        return articleService.findArticleByAuthorId(authorId);
    }
    /**
     * 更新所有文章的时间为新格式
     */
    /*@GetMapping("updateTime")
    public Result updateTime(){
        return articleService.updateTime();
    }*/
}
