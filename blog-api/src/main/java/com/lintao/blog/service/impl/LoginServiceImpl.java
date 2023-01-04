package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.LoginService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.utils.JWTUtils;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录功能
     * 1.检查参数是否合法
     * 2.根据用户名和密码去user表中查询是否存在
     * 3.如果不存在即登录失败
     * 4.如果存在，使用jwt生成token返回给前端
     * 5.token放入redis当中，redis token存储用户信息，设置过期时间(登录认证的时候，先认证token字符串是否合法，去redis认证是否存在)
     * @param loginParam
     * @return
     */
    @Override
    public Result login(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account)||StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUser(account,password);
        if (sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        return Result.success(token);
    }
}
