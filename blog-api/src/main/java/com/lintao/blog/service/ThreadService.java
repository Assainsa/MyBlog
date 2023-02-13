package com.lintao.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.mapper.SysUserMapper;
import com.lintao.blog.dao.pojo.Article;
import com.lintao.blog.dao.pojo.ArticleBody;
import com.lintao.blog.dao.pojo.Comment;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.utils.QiniuUtils;
import com.lintao.blog.vo.ArticleMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private QiniuUtils qiniuUtils;
    /**
     * 标识更新操作放入线程池中
     * 异步更新能够保证主线程的速度不会被影响
     * @param articleMapper
     * @param article
     */
    @Async("taskExecutor")
    public void updateArticleViewCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        int viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        //创建一个新对象来更新特定的属性
        articleUpdate.setViewCounts(viewCounts + 1);
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<Article>();
        updateWrapper.eq(Article::getId,article.getId());
        //为了多线程安全，这里使用了乐观锁，即只有在viewCount符合预期情况下才进行更新
        updateWrapper.eq(Article::getViewCounts,viewCounts);
        articleMapper.update(articleUpdate,updateWrapper);
        //发一条信息给rocketmq，当前文章更新了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(article.getId());
        rocketMQTemplate.convertAndSend("blog-update-article",articleMessage);
    }

    /**
     * 更新评论数
     * @param comment
     * @param delete
     */
    @Async("taskExecutor")
    public void updateCommentCounts(Comment comment, boolean delete) {
        Article article = articleMapper.selectById(comment.getArticleId());
        Integer commentCounts = article.getCommentCounts();
        Article articleUpdate = new Article();
        if (delete) {
            articleUpdate.setCommentCounts(commentCounts-1);
        }else {
            articleUpdate.setCommentCounts(commentCounts + 1);
        }
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        updateWrapper.eq(Article::getCommentCounts,commentCounts);
        articleMapper.update(articleUpdate,updateWrapper);
        //发一条信息给rocketmq，当前文章更新了，更新一下缓存
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(article.getId());
        rocketMQTemplate.convertAndSend("blog-update-article",articleMessage);
    }

    /**
     * 更新用户登录时间
     * @param sysUser
     */
    @Async("taskExecutor")
    public void updateLastLogin(SysUser sysUser) {
        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setLastLogin(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())));
        sysUserMapper.updateById(updateUser);
    }

    /**
     *删除文章里的图片
     * @param articleBody
     */
    @Async("taskExecutor")
    public void deleteImages(ArticleBody articleBody){
        String content = articleBody.getContent();
        String[] fileList = StringUtils.substringsBetween(content, ".com/", ")");
        qiniuUtils.deleteFiles(fileList);
    }
}
