package com.scramble.Scheduler;

import com.scramble.entity.Article;
import com.scramble.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ViewSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleRepository articleRepository;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void syncViewsToDB() {

        Set<String> keys = redisTemplate.keys("article:view:*");

        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {

            Long articleId = Long.parseLong(key.split(":")[2]);

            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) continue;

            long views = Long.parseLong(value.toString());

            Article article = articleRepository.findById(articleId).orElse(null);

            if (article != null) {
                article.setViewCount(article.getViewCount() + views);
                articleRepository.save(article);
            }

            // ✅ Clear Redis after syncing
            redisTemplate.delete(key);
        }
    }
}