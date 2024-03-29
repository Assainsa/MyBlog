package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lintao.blog.dao.mapper.ArticleTagMapper;
import com.lintao.blog.dao.mapper.TagMapper;
import com.lintao.blog.dao.pojo.ArticleTag;
import com.lintao.blog.dao.pojo.Tag;
import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.ArticleMessage;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.TagVo;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
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

    /**
     * 返回最热标签
     * 1.标签所拥有的文章数量最大：最热标签
     * 2.查询对tag_id进行分组计数，并求前limit个
     * 3.然后用tag_id去找tags
     * @param limit
     * @return
     */
    @Override
    public Result hots(int limit) {
        List<Long> tagIds = tagMapper.findHostTagIds(limit);
        if (CollectionUtils.isEmpty(tagIds)){
            return Result.success(Collections.emptyList());
        }
        List<Tag> tagList = tagMapper.findTagsByTagIds(tagIds);
        return Result.success(tagList);
    }

    @Override
    public Result findAll() {
        List<Tag> tags = tagMapper.selectList(null);
        return Result.success(copyList(tags));
    }

    @Override
    public Result findTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        return Result.success(copy(tag));
    }

    @Override
    public Result addTag(String tagName) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getTagName,tagName);
        Tag exist = tagMapper.selectOne(queryWrapper);
        if (exist!=null){
            return Result.fail(ErrorCode.ALREADY_EXIST.getCode(), ErrorCode.ALREADY_EXIST.getMsg());
        }
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setAvatar("/static/tag/tag.png");
        tagMapper.insert(tag);
        return Result.success(null);
    }

    @Override
    public Result deleteTag(Long tagId) {
        LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleTag::getTagId,tagId);
        articleTagMapper.delete(queryWrapper);
        tagMapper.deleteById(tagId);
        //发一条信息给rocketmq，当前文章更新/发布了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        rocketMQTemplate.convertAndSend("blog-update-article",articleMessage);
        return Result.success(null);
    }
}
