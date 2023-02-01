package com.lintao.blog.common.cache;

import java.lang.annotation.*;

/**
 * 统一缓存切点
 */
@Target({ElementType.METHOD})   //Method代表可以放在方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    long expire() default 1*60*1000;
    //缓存标识 key
    String name() default "";
}
