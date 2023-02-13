package com.lintao.blog.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lintao.blog.service.ArticleService;
import com.lintao.blog.service.TagService;
import com.lintao.blog.vo.ArticleMessage;
import com.lintao.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RocketMQMessageListener(topic = "blog-update-article",consumerGroup = "blog-update-article-group")
public class ArticleListener implements RocketMQListener<ArticleMessage> {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private TagService tagService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private long expire = 5*60*1000;

    /**
     * 更新缓存
     * @param articleMessage
     */
    @Override
    public void onMessage(ArticleMessage articleMessage) {
        try {
            //更新文章详情缓存
            Long articleId = articleMessage.getArticleId();
            ObjectMapper objectMapper = new ObjectMapper();
            String redisKey;
            if (articleId!=null) {
                String params = DigestUtils.md5Hex(articleId.toString());
                redisKey = "view_article::ArticleController::findArticleById::" + params;
                Result article = articleService.findArticleById(articleId);
                redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(article), Duration.ofMillis(expire));
                log.info("更新了文章详情缓存：{}", redisKey);
            }
            //更新最热文章
            Result hotArticle = articleService.hotArticle(5);
            redisKey = "hot_article::ArticleController::hotArticle::";
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(hotArticle), Duration.ofMillis(expire));
            //更新最新文章
            Result newArticle = articleService.newArticles(5);
            redisKey = "new_article::ArticleController::newArticles::";
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(newArticle), Duration.ofMillis(expire));
            //更新文章归档
            Result archives = articleService.listArchives();
            redisKey = "list_Archives::ArticleController::listArchives::";
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(archives), Duration.ofMillis(expire));
            //更新最热标签
            Result hots = tagService.hots(6);
            redisKey = "hot_tag::TagsController::hot::";
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(hots), Duration.ofMillis(expire));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
