package com.scramble.repository;

import com.scramble.entity.Clap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClapRepository extends JpaRepository<Clap, Long> {
    Optional<Clap> findByUserIdAndArticleId(Long userId, Long articleId);
    int countByArticleId(Long articleId);
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
}
