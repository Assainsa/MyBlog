package com.lintao.blog.handler;

import com.alibaba.fastjson.JSON;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.TokenService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UpdateTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            //handler可能是访问静态资源的，可以予以放行
            return true;
        }
        String token = request.getHeader("Authorization");
        log.info("==============================request start==================================");
        log.info("request uri:{}",request.getRequestURI());
        log.info("request method:{}",request.getMethod());
        log.info("token:{}",token);
        log.info("==============================request end==================================");
        SysUser sysUser = tokenService.checkToken(token);
        if (sysUser == null){    //token认证失败则返回包含未登录的json
            return true;
        }
        //更新token有效时间
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS); //这里用fastJSON来转换用户信息
        UserThreadLocal.put(sysUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}
