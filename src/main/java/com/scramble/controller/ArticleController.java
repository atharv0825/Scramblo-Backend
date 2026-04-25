package com.scramble.controller;

import com.scramble.dto.article.ArticleResponse;
import com.scramble.dto.article.CreateArticleRequest;
import com.scramble.dto.article.PageResponse;
import com.scramble.service.AISummaryService;
import com.scramble.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final AISummaryService aiSummaryService;

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(@RequestBody CreateArticleRequest request ) {
        return ResponseEntity.ok(articleService.articleResponse(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/recent")
    public ResponseEntity<PageResponse<ArticleResponse>> getRecentArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.getRecentArticles(page, size));
    }

    @PostMapping("/summarize")
    public ResponseEntity<String> summarize(@RequestBody String content) {
        return ResponseEntity.ok(aiSummaryService.generateSummary(content));
    }

    @GetMapping("/feed/following")
    public ResponseEntity<PageResponse<ArticleResponse>> getFollowingFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.getFollowingFeed(page, size));
    }

    @GetMapping("/feed")
    public ResponseEntity<PageResponse<ArticleResponse>> getHybridFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.getHybridFeed(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ArticleResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.searchArticles(query, page, size));
    }

    @GetMapping("/trending")
    public ResponseEntity<PageResponse<ArticleResponse>> getTrending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(articleService.getTrending(page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<ArticleResponse>> getArticlesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.getPublishedArticlesByUser(userId, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok("Article deleted successfully");
    }

}