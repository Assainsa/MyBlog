package com.lintao.blog.vo;

public enum ErrorCode {
    PARAMS_ERROR(10001,"参数有误"),
    ACCOUNT_PWD_NOT_EXIST(10002,"用户名或密码不存在"),
    TOKEN_INVALID(10003,"token不合法"),
    NO_PERMISSION(70001,"无访问权限"),
    SESSION_TIME_OUT(90001,"会话超时"),
    NO_LOGIN(90002,"未登录"),
    ACCOUNT_EXIST(10004,"用户名已存在"),
    UPLOAD_FAILED(20001,"上传失败"),
    SYSTEM_ERROR(-999,"系统错误"),
    ALREADY_EXIST(10005,"已存在相同标签"),
    NICKNAME_EXIST(10006,"昵称已存在");


    private int code;
    private String msg;
    ErrorCode(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
