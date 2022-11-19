package com.lintao.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lintao.blog.dao.pojo.Tag;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 根据文章id查询标签列表
     * @param articleId
     * @return
     */
    List<Tag> findTagsByArticleId(Long articleId);

    /**
     * 查询前n条最热标签
     * @param limit
     * @return
     */
    List<Long> findHostTagIds(int limit);

    /**
     * 根据id查tags
     * @return
     */
    List<Tag> findTagsByTagIds(List<Long> tagIds);
}
