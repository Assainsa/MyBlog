package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lintao.blog.dao.mapper.CommentMapper;
import com.lintao.blog.dao.pojo.Comment;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.CommentService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.ThreadService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ArticleMessage;
import com.lintao.blog.vo.CommentVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.UserVo;
import com.lintao.blog.vo.params.CommentParam;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ThreadService threadService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 1. 根据文章id查询评论列表，从comment表中获取
     * 2. 根据作者的id查询作者的信息
     * 3. 判断 如果level=1 要去查询它是否有子评论
     * 4. 如果有，根据评论id进行查询(parent_id)
     * @param id
     * @return
     */
    @Override
    public Result commentsByArticleId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>();
        queryWrapper.eq(Comment::getArticleId,id);
        queryWrapper.eq(Comment::getLevel,1);
        queryWrapper.orderByDesc(Comment::getCreateDate);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        List<CommentVo> commentVoList = copyList(comments);
        return Result.success(commentVoList);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        comment.setCreateDate(Long.valueOf(dateFormat.format(System.currentTimeMillis())));
        Long parent = commentParam.getParent();
        if (parent==null||parent==0){
            comment.setLevel(1);
        }else {
            comment.setLevel(2);
        }
        comment.setParentId(parent==null?0:parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId==null?0:toUserId);
        commentMapper.insert(comment);
        //更新文章的评论数
        threadService.updateCommentCounts(comment,false);
        //发一条信息给rocketmq，当前评论更新/发布了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(comment.getArticleId());
        rocketMQTemplate.convertAndSend("blog-update-comment",articleMessage);
        return Result.success(copy(comment));
    }

    @Override
    public Result deleteCommentByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,articleId);
        commentMapper.delete(queryWrapper);
        return Result.success(null);
    }

    @Override
    public Result deleteCommentById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment.getLevel()==2){
            //被删除的是子评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getId,comment.getParentId());
            Comment parent = commentMapper.selectOne(queryWrapper);
            queryWrapper.clear();
            queryWrapper.eq(Comment::getParentId,parent.getId());
            List<Comment> children = commentMapper.selectList(queryWrapper);
            //如果父评论已经被删除并且没有子评论，那么直接把这个父评论删除
            if (parent.getDeleted()==1&&children.size()==1){
                commentMapper.deleteById(parent.getId());
            }
            //然后再把子评论删除
            commentMapper.deleteById(id);
        }else {
            //被删除的是父评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getParentId,comment.getId());
            List<Comment> children = commentMapper.selectList(queryWrapper);
            //如果已经没有子评论，直接删除
            if (children.size()==0){
                commentMapper.deleteById(comment.getId());
            }else {
                //如果还有子评论，保留父评论
                comment.setDeleted(1);
                comment.setContent("**评论已经被删除**");
                commentMapper.updateById(comment);
            }
        }
        //更新文章评论数
        threadService.updateCommentCounts(comment,true);
        //发一条信息给rocketmq，当前评论更新/发布了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(comment.getArticleId());
        rocketMQTemplate.convertAndSend("blog-update-comment",articleMessage);
        return Result.success(null);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        List<CommentVo> commentVos = new ArrayList<>();
        for (Comment comment:comments){
            commentVos.add(copy(comment));
        }
        return commentVos;
    }

    private CommentVo copy(Comment comment){
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
        if (comment.getCreateDate()!=null){
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyyMMddHHmmss").parse(String.valueOf(comment.getCreateDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            commentVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        }
        //获取作者信息
        Long authorId = comment.getAuthorId();
        commentVo.setAuthor(sysUserService.findUserVoById(authorId));
        Integer level = comment.getLevel();
        //获取子评论
        if (1==level){
            Long id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //如果level不是1，则查是给谁评论
        if (level>1){
            Long toUid = comment.getToUid();
            UserVo userVoById = sysUserService.findUserVoById(toUid);
            commentVo.setToUser(userVoById);
        }
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId,id);
        queryWrapper.eq(Comment::getLevel,2);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        return copyList(comments);
    }
}
