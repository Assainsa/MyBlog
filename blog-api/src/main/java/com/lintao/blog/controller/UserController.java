package com.lintao.blog.controller;

import com.lintao.blog.common.aop.LogAnnotation;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 根据token获得用户信息
     * 注意这里前端是将token放在header里进行返回
     * @param token
     * @return
     */
    @GetMapping("currentUser")
    @LogAnnotation(module = "用户", operator = "获取用户信息")
    public Result currentUser(@RequestHeader("Authorization") String token){
        return sysUserService.findUserByToken(token);
    }

    @PostMapping("update")
    @LogAnnotation(module = "用户", operator = "更新用户信息")
    public Result updateUser(@RequestBody LoginParam user, @RequestHeader("Authorization") String token){
        return sysUserService.updateUser(user,token);
    }
}
