package com.lintao.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.LoginService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.ThreadService;
import com.lintao.blog.utils.JWTUtils;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.params.LoginParam;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ThreadService threadService;
    private static final String salt = "mszlu!@#"; //登录加密盐，用来加强password的md5加密效果
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
        password= DigestUtils.md5Hex(password + salt);  //对密码进行加密处理
        SysUser sysUser = sysUserService.findUser(account,password);
        if (sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS); //这里用fastJSON来转换用户信息
        //在线程池里更新lastLoginDate
        threadService.updateLastLogin(sysUser);
        return Result.success(token);
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1. 判断参数是否合法
         * 2. 判断账户是否存在，若存在则返回账户已被注册
         * 3. 不存在，注册用户
         * 4. 生成token
         * 5. 存入redis，并返回
         * 6. 注意加上事务，一旦中间任何过程出现问题，注册的用户需要回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if (StringUtils.isBlank(account)||StringUtils.isBlank(password)||StringUtils.isBlank(nickname)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if (sysUser != null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+salt));
        sysUser.setCreateDate(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())));
        sysUser.setLastLogin(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())));
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        sysUserService.save(sysUser);

        String token = JWTUtils.createToken(sysUser.getId());

        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1,TimeUnit.DAYS);
        return Result.success(token);
    }
}
