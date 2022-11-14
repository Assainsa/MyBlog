package com.lintao.blog.service;

import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.PageParams;

public interface ArticleService {

    /**
     * 分页查询 文章列表
     * @param pageParams
     * @return
     */
    Result listArticle(PageParams pageParams);
}
