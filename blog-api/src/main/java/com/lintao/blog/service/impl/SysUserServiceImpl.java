package com.lintao.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.mapper.SysUserMapper;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.LoginService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.TokenService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.LoginUserVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.UserVo;
import com.lintao.blog.vo.params.LoginParam;
import com.lintao.blog.vo.params.UserParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String salt = "mszlu!@#"; //登录加密盐，用来加强password的md5加密效果
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null){
            sysUser=new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("无");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        SysUser sysUser = tokenService.checkToken(token);
        if (sysUser == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMsg());
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId(sysUser.getId());
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setAvatar(sysUser.getAvatar());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户时id会自动生成，这个地方默认的id时分布式id，采用雪花算法
        sysUserMapper.insert(sysUser);
    }

    @Override
    public UserVo findUserVoById(Long authorId) {
        SysUser user = findUserById(authorId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);
        return userVo;
    }

    @Override
    public Result updateUser(LoginParam user, String token) {
        /**
         * 1. 判断参数是否合法
         * 2. 判断账户是否存在，若存在则返回账户已被注册
         * 3. 不存在，更新用户
         */

        String account = user.getAccount();
        String password = user.getPassword();
        String nickname = user.getNickname();
        if (StringUtils.isBlank(account)||StringUtils.isBlank(password)||StringUtils.isBlank(nickname)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser existUser = findUserByAccount(account);
        if (existUser!=null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),ErrorCode.ACCOUNT_EXIST.getMsg());
        }

        //要从threadlocal里拿用户信息，所以该controller方法要放入登录拦截器中
        SysUser updateUser = new SysUser();
        updateUser.setId(UserThreadLocal.get().getId());
        updateUser.setAccount(user.getAccount());
        updateUser.setPassword(DigestUtils.md5Hex(user.getPassword()+salt));
        updateUser.setNickname(user.getNickname());
        updateUser.setLastLogin(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())));
        sysUserMapper.updateById(updateUser);
        //更新ThreadLocal
        SysUser sysUser = sysUserMapper.selectById(UserThreadLocal.get().getId());
        UserThreadLocal.remove();
        UserThreadLocal.put(sysUser);
        //更新token
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS); //这里用fastJSON来转换用户信息
        return Result.success(null);
    }
}
