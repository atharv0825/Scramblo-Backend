package com.scramble.kafka;

import com.scramble.entity.Article;
import com.scramble.entity.SummaryStatus;
import com.scramble.repository.ArticleRepository;
import com.scramble.service.AISummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AISummaryConsumer {

    private final ArticleRepository articleRepository;
    private final AISummaryService aiSummaryService;

    @KafkaListener(topics = "GENERATE-SUMMARY" , groupId = "ai-summary-group")
    private void consume(Long articleId){
        log.info("Received from kafka : " + articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        try{
            String summary = aiSummaryService.generateSummary(article.getContent());
            if (summary.length() > 1000) {
                summary = summary.substring(0, 1000);
            }
            article.setSummary(summary);
            article.setSummaryStatus(SummaryStatus.GENERATED);
            articleRepository.save(article);
            log.info("Summary generated for articleID : " + articleId);
        }catch (Exception e){
            article.setSummaryStatus(SummaryStatus.FAILED);
            articleRepository.save(article);
            log.info("Summary generation FAILED");
            e.printStackTrace();
        }
    }


    @Scheduled(fixedDelay = 300000)
    public void retryOneArticle() {

        log.info("⏱️ Checking for ONE article to retry...");

        Optional<Article> optionalArticle =
                articleRepository.findTopBySummaryStatusInOrderByCreatedAtAsc(
                        List.of(SummaryStatus.FAILED, SummaryStatus.PENDING)
                );

        if (optionalArticle.isEmpty()) {
            log.info("No article found for retry");
            return;
        }

        Article article = optionalArticle.get();

        try {

            if (article.getRetryCount() >= 3) {
                log.warn("Max retries reached for article: {}", article.getId());
                return;
            }

            log.info("Retrying article: {}", article.getId());

            String summary = aiSummaryService.generateSummary(article.getContent());

            if (summary.length() > 1000) {
                summary = summary.substring(0, 1000);
            }

            article.setSummary(summary);
            article.setSummaryStatus(SummaryStatus.GENERATED);
            article.setRetryCount(article.getRetryCount() + 1);

            log.info("Retry SUCCESS for article: {}", article.getId());

        } catch (Exception e) {

            article.setSummaryStatus(SummaryStatus.FAILED);
            article.setRetryCount(article.getRetryCount() + 1);

            log.error("Retry FAILED for article: {}", article.getId(), e);
        }

        articleRepository.save(article);
    }
}
