package com.lintao.blog.service;

import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.UserVo;
import com.lintao.blog.vo.params.LoginParam;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SysUserService{
    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    /**
     * 根据token查询用户信息
     * @param token
     * @return
     */
    Result findUserByToken(String token);

    /**
     * 根据account查找用户
     * @param account
     * @return
     */
    SysUser findUserByAccount(String account);

    /**
     * 保存用户
     * @param sysUser
     */
    void save(SysUser sysUser);

    UserVo findUserVoById(Long authorId);

    Result updateUser(LoginParam user, String token);

    SysUser findUserByNickName(String nickname);
}
