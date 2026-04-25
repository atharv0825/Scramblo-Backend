package com.scramble.repository;

import com.scramble.entity.Article;
import com.scramble.entity.ArticleStatus;
import com.scramble.entity.SummaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("""
    SELECT a FROM Article a
    WHERE a.author.id IN :followingIds
    AND a.status = 'PUBLISHED'
    ORDER BY a.createdAt DESC
""")
    Page<Article> findFollowingFeed(List<Long> followingIds, Pageable pageable);

    @Query("""
    SELECT DISTINCT a FROM Article a
    LEFT JOIN ArticleTag at ON at.article.id = a.id
    LEFT JOIN Tag t ON t.id = at.tag.id
    WHERE 
        (
            a.author.id IN :followingIds
            OR t.name IN :tags
        )
    AND a.status = 'PUBLISHED'
    ORDER BY a.createdAt DESC
""")
    Page<Article> findHybridFeed(
            List<Long> followingIds,
            List<String> tags,
            Pageable pageable
    );

    @Query("""
    SELECT a FROM Article a
    JOIN ArticleTag at ON at.article.id = a.id
    JOIN Tag t ON t.id = at.tag.id
    WHERE t.name IN :tags
    GROUP BY a.id
    ORDER BY COUNT(t.id) DESC, a.createdAt DESC
""")
    Page<Article> searchByTags(@Param("tags") List<String> tags, Pageable pageable);

    Optional<Article> findTopBySummaryStatusInOrderByCreatedAtAsc(
            List<SummaryStatus> statuses
    );

    @Query("""
    SELECT DISTINCT a FROM Article a
    LEFT JOIN ArticleTag at ON at.article.id = a.id
    LEFT JOIN Tag t ON t.id = at.tag.id
    WHERE 
        a.status = 'PUBLISHED'
        AND (
            LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    ORDER BY a.createdAt DESC
""")
    Page<Article> searchArticles(@Param("search") String search, Pageable pageable);


    @Query("""
        SELECT a FROM Article a
        WHERE a.status = 'PUBLISHED'
        ORDER BY a.createdAt DESC
        """)
    Page<Article> findRecentArticles(Pageable pageable);

    Page<Article> findByAuthorIdAndStatus(Long userId, ArticleStatus status, Pageable pageable);



    @Query("""
        SELECT a FROM Article a
        WHERE a.status = 'PUBLISHED'
        AND a.createdAt >= :time
        ORDER BY (a.viewCount + a.clapCount * 2 + a.commentCount * 3) DESC
        """)
    Page<Article> findTrendingByTime(LocalDateTime time, Pageable pageable);




}
