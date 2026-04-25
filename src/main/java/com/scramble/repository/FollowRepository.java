package com.scramble.repository;

import com.scramble.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow , Long> {
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowerId(Long followerId);
    List<Follow> findByFollowingId(Long followingId);

    Boolean existsByFollowerIdAndFollowingId(Long id, Long userId);
}
