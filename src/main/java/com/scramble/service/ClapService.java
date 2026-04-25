package com.scramble.service;

import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.entity.Article;
import com.scramble.entity.Clap;
import com.scramble.entity.User;
import com.scramble.kafka.NotificationProducer;
import com.scramble.repository.ArticleRepository;
import com.scramble.repository.ClapRepository;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ClapService {

    private final ClapRepository clapRepository;
    private final ArticleRepository articleRepository;
    private final SecurityUtils securityUtils;
    private final NotificationProducer notificationProducer;

    @Transactional
    public boolean toggleClap(Long articleId){

        User user = securityUtils.getCurrentUser();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Clap existing = clapRepository
                .findByUserIdAndArticleId(user.getId(), articleId)
                .orElse(null);


        if (existing != null) {

            clapRepository.delete(existing);

            article.setClapCount(
                    Math.max(0, article.getClapCount() - 1)
            );

            return false; // now unliked
        }

        Clap clap = Clap.builder()
                .user(user)
                .article(article)
                .count(1)
                .build();

        clapRepository.save(clap);

        article.setClapCount(article.getClapCount() + 1);

        // 🔔 Notification
        User author = article.getAuthor();

        if (!author.getId().equals(user.getId())) {

            notificationProducer.sendEvent(
                    "LIKE",
                    NotificationEvent.builder()
                            .userId(author.getId())
                            .type("LIKE")
                            .message(user.getName() + " liked your article")
                            .articleId(article.getId())
                            .build()
            );
        }

        return true; // now liked
    }
}