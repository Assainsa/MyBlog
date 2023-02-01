package com.lintao.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lintao.blog.dao.mapper.CommentMapper;
import com.lintao.blog.dao.pojo.Comment;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.CommentService;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.ThreadService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.CommentVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.UserVo;
import com.lintao.blog.vo.params.CommentParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ThreadService threadService;

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
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        String formatTime = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        comment.setCreateDate(Long.valueOf(formatTime));
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
        threadService.updateCommentCounts(comment);
        return Result.success(copy(comment));
    }

    @Override
    public Result deleteCommentByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,articleId);
        commentMapper.delete(queryWrapper);
        return Result.success(null);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        List<CommentVo> commentVos = new ArrayList<>();
        for (Comment comment:comments){
            commentVos.add(copy(comment));
        }
        return commentVos;
    }

    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
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
