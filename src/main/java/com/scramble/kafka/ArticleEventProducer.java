package com.scramble.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleEventProducer {
    private final KafkaTemplate<String , Long>kafkaTemplate;

    private static final String TOPIC = "GENERATE-SUMMARY";

    public void sendSummary(Long articleId){
        kafkaTemplate.send(TOPIC,articleId);
        log.info("sent to kafka : ArticleId : " + articleId);
    }
}

