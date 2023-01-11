package com.lintao.blog.service;

import com.lintao.blog.dao.pojo.SysUser;

public interface SysUserService{
    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);
}
