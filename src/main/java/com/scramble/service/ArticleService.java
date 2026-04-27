package com.scramble.service;

import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.dto.User.UserResponse;
import com.scramble.dto.article.ArticleResponse;
import com.scramble.dto.article.CreateArticleRequest;
import com.scramble.dto.article.PageResponse;
import com.scramble.entity.*;
import com.scramble.kafka.ArticleEventProducer;
import com.scramble.kafka.NotificationProducer;
import com.scramble.repository.*;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final SecurityUtils securityUtils;
    private final ArticleTagRepository articleTagRepository;
    private final TagRepository tagRepository;
    private final ArticleEventProducer articleEventProducer;
    private final NotificationProducer notificationProducer;
    private final FollowRepository followRepository;
    private final UserService userInterestService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClapRepository clapRepository;
    private final BookmarkRepository bookmarkRepository;
    private final S3Service s3Service;

    public String getCacheKey(String type, int page, int size) {
        Long userId = securityUtils.getCurrentUser().getId();
        return type + ":" + userId + ":" + page + ":" + size;
    }

    @CacheEvict(value = {
            "followingFeed",
            "hybridFeed",
            "searchArticles"
    }, allEntries = true)
    public ArticleResponse articleResponse(CreateArticleRequest request) {

        User user = securityUtils.getCurrentUser();

        String safeHtml = Jsoup.clean(
                request.getContent(),
                Safelist.basicWithImages()
        );

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Title is required");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Content is required");
        }

        int words = request.getContent().split("\\s+").length;
        int readingTime = Math.max(1, words / 200);

        Article article = Article.builder()
                .title(request.getTitle().trim())
                .subtitle(request.getSubtitle())
                .content(safeHtml)
                .coverImage(request.getCoverImage())
                .readingTime(readingTime)
                .summaryStatus(SummaryStatus.PENDING)
                .status(ArticleStatus.valueOf(request.getStatus().toUpperCase()))
                .author(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Article savedArticle = articleRepository.save(article);
        articleEventProducer.sendSummary(savedArticle.getId());

        List<Follow> followers = followRepository
                .findByFollowingId(user.getId());

        for (Follow follow : followers) {

            notificationProducer.sendEvent(
                    "NEW_POST",
                    NotificationEvent.builder()
                            .userId(follow.getFollower().getId())
                            .type("NEW_POST")
                            .message("New post from " + user.getName())
                            .articleId(savedArticle.getId())
                            .build()
            );
        }


        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                String normalized = tagName.trim().toUpperCase();

                Tag tag = tagRepository.findByName(normalized)
                        .orElseGet(() -> tagRepository.save(Tag.builder()
                                .name(normalized)
                                .build()));

                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticle(savedArticle);
                articleTag.setTag(tag);

                articleTagRepository.save(articleTag);
            }
        }

        return mapToResponse(savedArticle);
    }


    public ArticleResponse getArticleById(Long id) {

        incrementView(id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        return mapToResponse(article);
    }


    @Cacheable(
            value = "followingFeed",
            key = "#root.target.getCacheKey('following', #page, #size)"
    )
    public PageResponse<ArticleResponse> getFollowingFeed(int page , int size){

        User user = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        List<Long> followingIds = getFollowingIds(user.getId());

        if (followingIds.isEmpty()) {
            return PageResponse.<ArticleResponse>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .build();
        }

        Page<Article> articles = articleRepository.findFollowingFeed(followingIds, pageable);

        return PageResponse.<ArticleResponse>builder()
                .content(articles.map(this::mapToResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(articles.getTotalElements())
                .build();
    }

    @Cacheable(
            value = "hybridFeed",
            key = "#root.target.getCacheKey('hybrid', #page, #size)"
    )
    public PageResponse<ArticleResponse> getHybridFeed(int page, int size) {

        User user = securityUtils.getCurrentUser();

        Pageable pageable = PageRequest.of(0, size * 3);

        List<Long> followingIds = getFollowingIds(user.getId());
        List<String> interests = userInterestService.getUserInterestTags(user.getId());

        List<Article> merged = new ArrayList<>();


        if (!followingIds.isEmpty()) {
            merged.addAll(
                    articleRepository.findFollowingFeed(followingIds, pageable).getContent()
            );
        }


        if (!interests.isEmpty()) {
            merged.addAll(
                    articleRepository.searchByTags(interests, pageable).getContent()
            );
        }

        merged.addAll(
                articleRepository.findRecentArticles(pageable).getContent()
        );


        Map<Long, Article> uniqueMap = new LinkedHashMap<>();
        for (Article article : merged) {
            uniqueMap.put(article.getId(), article);
        }

        List<Article> uniqueList = new ArrayList<>(uniqueMap.values());


        uniqueList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));


        int start = page * size;
        int end = Math.min(start + size, uniqueList.size());

        List<Article> paginated =
                start >= uniqueList.size() ? List.of() : uniqueList.subList(start, end);

        return PageResponse.<ArticleResponse>builder()
                .content(paginated.stream().map(this::mapToResponse).toList())
                .page(page)
                .size(size)
                .totalElements(uniqueList.size())
                .build();
    }

    @Cacheable(
            value = "searchArticles",
            key = "#search + ':' + #page + ':' + #size"
    )
    public PageResponse<ArticleResponse> searchArticles(String search, int page, int size) {

        if (search == null || search.isBlank()) {
            return PageResponse.<ArticleResponse>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles = articleRepository.searchArticles(search.trim(), pageable);

        return PageResponse.<ArticleResponse>builder()
                .content(articles.map(this::mapToResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(articles.getTotalElements())
                .build();
    }

    public void incrementView(Long articleId) {

        Long userId = securityUtils.getCurrentUser().getId();

        String viewedKey = "article:viewed:" + articleId;
        String countKey = "article:view:" + articleId;

        Boolean isNew = redisTemplate.opsForSet().add(viewedKey, userId.toString()) == 1;

        if (Boolean.TRUE.equals(isNew)) {
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.expire(viewedKey, Duration.ofHours(24));
            redisTemplate.expire(countKey, Duration.ofHours(24));
        }
    }

    @Cacheable(value = "trendingFeed", key = "#page + ':' + #size")
    public PageResponse<ArticleResponse> getTrending(int page, int size) {

        Pageable pageable = PageRequest.of  (page, size);

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        Page<Article> articles =
                articleRepository.findTrendingByTime(last24Hours,pageable);

        return PageResponse.<ArticleResponse>builder()
                .content(articles.map(this::mapToResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(articles.getTotalElements())
                .build();
    }

    public PageResponse<ArticleResponse> getPublishedArticlesByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articles = articleRepository
                .findByAuthorIdAndStatus(userId, ArticleStatus.PUBLISHED, pageable);

        return PageResponse.<ArticleResponse>builder()
                .content(articles.map(this::mapToResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(articles.getTotalElements())
                .build();
    }

    public PageResponse<ArticleResponse> getRecentArticles(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articles = articleRepository.findRecentArticles(pageable);

        return PageResponse.<ArticleResponse>builder()
                .content(articles.map(this::mapToResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(articles.getTotalElements())
                .build();
    }

    public void deleteArticle(Long articleId) {

        User currentUser = securityUtils.getCurrentUser();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this article");
        }

        // 🔥 delete S3 file
        if (article.getCoverImage() != null && !article.getCoverImage().isEmpty()) {
            s3Service.deleteFile(article.getCoverImage());
        }

        // 🔥 FIX: delete child records FIRST
        articleTagRepository.deleteByArticleId(articleId);

        // 🔥 delete parent
        articleRepository.delete(article);
    }

    private ArticleResponse mapToResponse(Article article) {

        User currentUser = null;
        boolean liked = false;
        boolean bookmarked = false;

        Object redisViewsObj = redisTemplate.opsForValue()
                .get("article:view:" + article.getId());

        long redisViews = 0;

        if (redisViewsObj instanceof Number) {
            redisViews = ((Number) redisViewsObj).longValue();
        }

        long totalViews = article.getViewCount() + redisViews;

        try {
            currentUser = securityUtils.getCurrentUser();
        } catch (Exception e) {
            // user not logged in → ignore
        }

        if (currentUser != null) {
            liked = clapRepository.existsByUserIdAndArticleId(
                    currentUser.getId(),
                    article.getId()
            );
            bookmarked = bookmarkRepository.existsByUserIdAndArticleId(
                    currentUser.getId(),
                    article.getId()
            );
        }

        return ArticleResponse.builder()
                .id(article.getId())
                .summaryStatus(article.getSummaryStatus().name())
                .status(article.getStatus())
                .title(article.getTitle())
                .coverImage(article.getCoverImage())
                .content(article.getContent())
                .summary(article.getSummary())
                .subtitle(article.getSubtitle())
                .readingTime(article.getReadingTime())
                .commentCount(article.getCommentCount())
                .clapCount(article.getClapCount())
                .viewCount(totalViews)
                .isLiked(liked)
                .bookmarked(bookmarked)
                .author(mapUser(article.getAuthor()))
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }



    private List<Long> getFollowingIds(Long userId) {
        return followRepository.findByFollowerId(userId)
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .toList();
    }

    private UserResponse mapUser(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .build();
    }

    

}
