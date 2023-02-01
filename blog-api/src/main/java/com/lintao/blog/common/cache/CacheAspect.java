package com.lintao.blog.common.cache;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 统一缓存切面
 * 实现了一个简单的springCache
 */
@Aspect
@Component
@Slf4j
public class CacheAspect {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Pointcut("@annotation(com.lintao.blog.common.cache.Cache)")
    public void pt(){};

    @Around("pt()")
    public Object around(ProceedingJoinPoint joinPoint){
        try{
            Signature signature = joinPoint.getSignature();
            //类名
            String className = joinPoint.getTarget().getClass().getSimpleName();
            //调用的方法名
            String methodName = signature.getName();

            //获取参数类型是为了getMethod()方法
            Class[] parameterTypes = new Class[joinPoint.getArgs().length];
            Object[] args = joinPoint.getArgs();
            //将参数转化为json字符串
            String params = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i]!=null){
                    params+= JSON.toJSONString(args[i]);
                    parameterTypes[i] = args[i].getClass();
                }else {
                    parameterTypes[i] = null;
                }
            }
            if (StringUtils.isNotEmpty(params)){
                //进行md5加密，以防止key过长以及字符转义获取不到的情况
                params = DigestUtils.md5Hex(params);
            }
            Method method = joinPoint.getSignature().getDeclaringType().getMethod(methodName, parameterTypes);
            //获取Cache注解
            Cache annotation = method.getAnnotation(Cache.class);
            //缓存过期时间
            long expire = annotation.expire();
            //缓存名称
            String name = annotation.name();
            //先尝试从redis中获取(key的组成是缓存名称+类名称+方法名称+参数md5加密，这样能保证唯一)
            String redisKey = name + "::" + className+"::"+methodName+"::"+params;
            String redisValue = redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotEmpty(redisValue)){
                log.info("从缓存中获取:{}.{}",className,methodName);
                return JSON.parseObject(redisValue, Result.class);
            }
            //如果redis里没有，就执行方法，然后把执行结果放入redis中
            ObjectMapper objectMapper = new ObjectMapper();
            Object result = joinPoint.proceed();
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(result), Duration.ofMillis(expire));
            log.info("存入缓存:{}.{}",className,methodName);
            return result;
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMsg());
    }
}
