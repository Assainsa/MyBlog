package com.lintao.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class LoginUserVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String account;
    private String nickname;
    private String avatar;
    private Integer admin;
}
