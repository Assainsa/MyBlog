package com.lintao.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
@Data
public class ArticleVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String summary;
    private Integer commentCounts;
    private Integer viewCounts;
    private Integer weight;
    private String createDate;
    private UserVo author;
    private ArticleBodyVo body;
    private List<TagVo> tags;
    private CategoryVo category;
}
