package com.lintao.blog.service;


import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.CommentParam;

public interface CommentService {

    /**
     * 根据文章id查询所有的评论列表
     * @param id
     * @return
     */
    Result commentsByArticleId(Long id);

    Result comment(CommentParam commentParam);

    Result deleteCommentByArticleId(Long articleId);
}
