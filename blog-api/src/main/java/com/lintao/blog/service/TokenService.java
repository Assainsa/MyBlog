package com.lintao.blog.service;

import com.lintao.blog.dao.pojo.SysUser;

public interface TokenService {
    SysUser checkToken(String token);
}
