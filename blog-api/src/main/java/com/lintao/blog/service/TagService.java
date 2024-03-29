package com.lintao.blog.service;

import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.TagVo;

import java.util.List;

public interface TagService {

    List<TagVo> findTagsByArticleId(Long articleId);

    Result hots(int limit);

    Result findAll();

    Result findTagById(Long id);

    Result addTag(String tagName);

    Result deleteTag(Long tagId);
}
