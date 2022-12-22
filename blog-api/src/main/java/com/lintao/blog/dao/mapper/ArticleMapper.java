package com.lintao.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lintao.blog.dao.dos.Archives;
import com.lintao.blog.dao.pojo.Article;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    List<Archives> listArchives();
}
