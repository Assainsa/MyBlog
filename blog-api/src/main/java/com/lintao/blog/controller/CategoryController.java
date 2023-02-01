package com.lintao.blog.controller;

import com.lintao.blog.service.CategoryService;
import com.lintao.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("categorys")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result categories(){
        return categoryService.findAll();
    }

    @GetMapping("detail")
    public Result detail(){
        return categoryService.findAll();
    }

    @GetMapping("detail/{id}")
    public Result detailById(@PathVariable("id") Long id){
        return categoryService.findCategoryDetailById(id);
    }
}
