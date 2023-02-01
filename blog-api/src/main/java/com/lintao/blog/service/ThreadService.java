package com.lintao.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.mapper.SysUserMapper;
import com.lintao.blog.dao.pojo.Article;
import com.lintao.blog.dao.pojo.Comment;
import com.lintao.blog.dao.pojo.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class ThreadService {
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    /**
     * 标识更新操作放入线程池中
     * 异步更新能够保证主线程的速度不会被影响
     * @param articleMapper
     * @param article
     */
    @Async("taskExecutor")
    public void updateArticleViewCount(Article article) {
        int viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        //创建一个新对象来更新特定的属性
        articleUpdate.setViewCounts(viewCounts+1);
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<Article>();
        updateWrapper.eq(Article::getId,article.getId());
        //为了多线程安全，这里使用了乐观锁，即只有在viewCount符合预期情况下才进行更新
        updateWrapper.eq(Article::getViewCounts,viewCounts);
        articleMapper.update(articleUpdate,updateWrapper);
    }

    @Async("taskExecutor")
    public void updateCommentCounts(Comment comment) {
        Article article = articleMapper.selectById(comment.getArticleId());
        Integer commentCounts = article.getCommentCounts();
        Article articleUpdate = new Article();
        articleUpdate.setCommentCounts(commentCounts+1);
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        updateWrapper.eq(Article::getCommentCounts,commentCounts);
        articleMapper.update(articleUpdate,updateWrapper);
    }

    @Async("taskExecutor")
    public void updateLastLogin(SysUser sysUser) {
        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setLastLogin(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())));
        sysUserMapper.updateById(updateUser);
    }
}
