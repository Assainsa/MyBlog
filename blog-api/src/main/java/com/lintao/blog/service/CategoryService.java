package com.lintao.blog.service;

import com.lintao.blog.vo.CategoryVo;
import com.lintao.blog.vo.Result;

public interface CategoryService {
    /**
     * 根据id查询类别
     * @param categoryId
     * @return
     */
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();
}
