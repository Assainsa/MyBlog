package com.lintao.blog.utils;

import com.lintao.blog.dao.pojo.SysUser;

/**
 * 线程变量隔离
 * 在一个线程里存储user信息，这个信息只能在这个线程里获取，其它线程无法获取
 * 单例设计模式
 */
public class UserThreadLocal {
    private UserThreadLocal(){};

    private static final ThreadLocal<SysUser> LOCAL = new ThreadLocal<>();

    public static void put(SysUser sysUser){
        LOCAL.set(sysUser);
    }

    public static SysUser get(){
        return LOCAL.get();
    }

    public static void remove(){
        LOCAL.remove();
    }
}
