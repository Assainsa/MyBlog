package com.lintao.blog.controller;

import com.lintao.blog.service.LoginService;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("register")
public class RegisterController {
    @Autowired
    private LoginService loginService;
    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso 单点登录，后期如果把登录注册功能提出去（单独的服务，可以独立提供接口服务）
        return loginService.register(loginParam);
    }

    @GetMapping("InvitationCode")
    public Result getInvitationCode(){
        return loginService.getInvitationCode();
    }
}
