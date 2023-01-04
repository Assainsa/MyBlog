package com.lintao.blog.service;

import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.LoginParam;

public interface LoginService {

    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);
}
