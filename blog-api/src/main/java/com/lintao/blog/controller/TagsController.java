package com.lintao.blog.controller;

import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
public class TagsController {

    @Autowired
    private TagService tagService;

    // /tags/hot
    @GetMapping("hot")
    public Result hot(){
        int limit=6;    //查询最热的6个标签
        return tagService.hots(limit);
    }
}
