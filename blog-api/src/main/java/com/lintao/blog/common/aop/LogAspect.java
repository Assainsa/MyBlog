package com.lintao.blog.common.aop;

import com.alibaba.fastjson.JSON;
import com.lintao.blog.utils.HttpContextUtils;
import com.lintao.blog.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 切面类
 */
@Component
@Aspect
@Slf4j
public class LogAspect {
    /**
     * 定义切入点方便复用
     */
    @Pointcut("@annotation(com.lintao.blog.common.aop.LogAnnotation)")
    public void pt(){}

    /**
     * 环绕通知
     */
    @Around("pt()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = joinPoint.proceed();
        long executeTime = System.currentTimeMillis() - beginTime;
        //保存日志
        recordLog(joinPoint, executeTime);
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation annotation = method.getAnnotation(LogAnnotation.class);
        //注解参数
        log.info("===================================log start===================================");
        log.info("module:{}",annotation.module());
        log.info("operator:{}",annotation.operator());

        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.info("request method:{}",className+"."+methodName+"()");

        //请求的参数
        Object[] args = joinPoint.getArgs();
        String params = JSON.toJSONString(args);
        log.info("params:{}",params);

        //获取request的IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.info("ip:{}", IpUtils.getIpAddr(request));

        log.info("execute time:{} ms",time);
        log.info("===================================end===================================");
    }
}
