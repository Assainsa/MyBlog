package com.lintao.blog.controller;

import com.lintao.blog.common.aop.LogAnnotation;
import com.lintao.blog.common.cache.Cache;
import com.lintao.blog.service.CommentService;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.CommentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comments")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @LogAnnotation(module = "评论",operator = "获取文章评论")
    @Cache(expire = 5*60*1000,name = "article_comment")
    @GetMapping("article/{id}")
    public Result comments(@PathVariable("id") Long id){
        return commentService.commentsByArticleId(id);
    }

    @PostMapping("create/change")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentService.comment(commentParam);
    }

    @GetMapping("delete/{id}")
    public Result deleteCommentById(@PathVariable("id") Long id){
        return commentService.deleteCommentById(id);
    }
}
