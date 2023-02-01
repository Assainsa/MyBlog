package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lintao.blog.dao.mapper.CategoryMapper;
import com.lintao.blog.dao.pojo.Category;
import com.lintao.blog.service.CategoryService;
import com.lintao.blog.vo.CategoryVo;
import com.lintao.blog.vo.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson.JSONPatch.OperationType.copy;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public CategoryVo findCategoryById(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        return copy(category);
    }

    @Override
    public Result findAll() {
        List<Category> categories = categoryMapper.selectList(null);
        return Result.success(copyList(categories));
    }

    @Override
    public Result findCategoryDetailById(Long id) {
        return Result.success(findCategoryById(id));
    }

    private CategoryVo copy(Category category){
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category,categoryVo);
        return categoryVo;
    }

    private List<CategoryVo> copyList(List<Category> categoryList){
        List<CategoryVo> categoryVos = new ArrayList<CategoryVo>();
        for (Category category : categoryList){
            categoryVos.add(copy(category));
        }
        return categoryVos;
    }
}
