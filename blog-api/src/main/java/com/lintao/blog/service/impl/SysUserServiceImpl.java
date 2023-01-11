package com.lintao.blog.service.impl;

import com.lintao.blog.dao.mapper.SysUserMapper;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null){
            sysUser=new SysUser();
            sysUser.setNickname("无");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        return null;
    }
}
