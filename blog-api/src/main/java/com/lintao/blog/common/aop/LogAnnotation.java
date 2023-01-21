package com.lintao.blog.common.aop;

import java.lang.annotation.*;

/**
 * AOP日志，通过定位连接点来记录日志
 */
@Target({ElementType.METHOD})   //Method代表可以放在方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    String module() default "";
    String operator() default "";
}
