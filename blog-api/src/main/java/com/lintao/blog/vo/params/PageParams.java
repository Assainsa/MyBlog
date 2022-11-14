package com.lintao.blog.vo.params;

import lombok.Data;

/**
 * 用来存放前端需要展示的数据
 * 这里是分页用到的数据
 */
@Data
public class PageParams {
    //当前页数
    private int page = 1;
    //每页查询记录数量
    private int pageSize = 10;
}
