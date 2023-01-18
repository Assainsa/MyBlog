package com.lintao.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lintao.blog.dao.mapper.ArticleMapper;
import com.lintao.blog.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ThreadService {
    /**
     * 标识更新操作放入线程池中
     * 异步更新能够保证主线程的速度不会被影响
     * @param articleMapper
     * @param article
     */
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
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
}
