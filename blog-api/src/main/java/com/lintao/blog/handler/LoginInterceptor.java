package com.lintao.blog.handler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.LoginService;
import com.lintao.blog.service.TokenService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;
    /**
     * 在执行controller方法之前判断是否登录
     * 1. 要判断请求的接口路径是否为HandlerMethod（controller方法）
     * 2. 需要判断token是否为空，如果为空则未登录
     * 3. 如果token不为空，登录验证loginService checkToken
     * 4. 认证成功可以直接放行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
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
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), ErrorCode.NO_LOGIN.getMsg());
            response.setContentType("text/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        UserThreadLocal.put(sysUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果不删除ThreadLocal中用完的信息，会有内存泄露的风险
        UserThreadLocal.remove();
    }
}
