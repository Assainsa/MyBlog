package com.lintao.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.TokenService;
import com.lintao.blog.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Override
    public SysUser checkToken(String token) {
        /**
         * 1. token合法性校验
         *  是否为空，解析是否成功，redis是否存在
         * 2. 如果校验失败，返回错误
         * 3. 如果成功，返回对应的结果 LoginUserVo
         */
        if (StringUtils.isBlank(token)){
            return null;
        }
        Map<String, Object> stringObjectMap =
                JWTUtils.checkToken(token);
        if (stringObjectMap==null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)){
            return null;
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);//通过合法性校验，直接返回user对象
        return sysUser;
    }
}
