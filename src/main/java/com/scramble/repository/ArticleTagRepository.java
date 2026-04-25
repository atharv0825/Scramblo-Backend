package com.scramble.repository;

import com.scramble.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleTag at WHERE at.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
