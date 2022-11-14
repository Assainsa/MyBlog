package com.lintao.blog.service.impl;

import com.lintao.blog.dao.mapper.TagMapper;
import com.lintao.blog.dao.pojo.Tag;
import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.TagVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        List<Tag> tags = tagMapper.findTagsByArticleId(articleId);
        return copyList(tags);
    }

    public List<TagVo> copyList(List<Tag> tagList){
        ArrayList<TagVo> tagVos = new ArrayList<>();
        for (Tag tag:tagList){
            tagVos.add(copy(tag));
        }
        return tagVos;
    }
    public TagVo copy(Tag tag){
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag,tagVo);
        return tagVo;
    }
}
