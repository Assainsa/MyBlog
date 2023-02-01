package com.lintao.blog.controller;

import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public Result findAll(){
        return tagService.findAll();
    }

    @GetMapping("detail")
    public Result detail(){
        return tagService.findAll();
    }

    @GetMapping("detail/{id}")
    public Result detailById(@PathVariable("id") Long id){
        return tagService.findTagById(id);
    }

    @GetMapping("add/{tagName}")
    public Result addTag(@PathVariable("tagName") String tagName){
        return tagService.addTag(tagName);
    }
}
