package com.scramble.kafka;

import com.scramble.dto.article.ArticlePublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticlePublishedProducer {

    private final KafkaTemplate<String, ArticlePublishedEvent> kafkaTemplate;

    private static final String TOPIC = "ARTICLE_PUBLISHED";

    public void publish(ArticlePublishedEvent event){

        kafkaTemplate.send(
                TOPIC,
                event.getArticleId().toString(),
                event
        );

        log.info(
                "Published article event {}",
                event.getArticleId()
        );
    }
}