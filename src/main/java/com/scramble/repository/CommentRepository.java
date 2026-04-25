package com.scramble.repository;

import com.scramble.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByArticleId(Long articleId);
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);
}
