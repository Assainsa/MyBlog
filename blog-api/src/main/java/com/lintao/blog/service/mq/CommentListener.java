package com.lintao.blog.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lintao.blog.service.CommentService;
import com.lintao.blog.vo.ArticleMessage;
import com.lintao.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RocketMQMessageListener(topic = "blog-update-comment",consumerGroup = "blog-update-comment-group")
public class CommentListener implements RocketMQListener<ArticleMessage> {
    @Autowired
    private CommentService commentService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private long expire = 5*60*1000;

    @Override
    public void onMessage(ArticleMessage articleMessage) {
        try {
            //更新文章评论的缓存
            Long articleId = articleMessage.getArticleId();
            ObjectMapper objectMapper = new ObjectMapper();
            String redisKey;
            if (articleId!=null) {
                String params = DigestUtils.md5Hex(articleId.toString());
                redisKey = "article_comment::CommentController::comments::" + params;
                Result comments = commentService.commentsByArticleId(articleId);
                redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(comments), Duration.ofMillis(expire));
                log.info("更新了文章评论缓存：{}", redisKey);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
