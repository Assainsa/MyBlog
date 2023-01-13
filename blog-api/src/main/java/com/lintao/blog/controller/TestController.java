package com.lintao.blog.controller;

import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
