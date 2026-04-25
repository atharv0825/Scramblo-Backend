package com.scramble.service;

import com.scramble.dto.Follow.FollowResponse;
import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.entity.Follow;
import com.scramble.entity.User;
import com.scramble.kafka.NotificationProducer;
import com.scramble.repository.FollowRepository;
import com.scramble.repository.UserRepository;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final NotificationProducer notificationProducer;

    @Transactional
    @CacheEvict(value = {"followingFeed", "hybridFeed"}, allEntries = true)
    public void follow(Long userId){

        User user = securityUtils.getCurrentUser();

        if(user == null){
            throw new RuntimeException("User not authenticated");
        }

        if(user.getId().equals(userId)){
            throw new RuntimeException("You cannot follow yourself");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(user.getId(), userId)
                .orElse(null);

        if(follow != null){
            // UNFOLLOW
            followRepository.delete(follow);

            user.setFollowingCount(Math.max(0, user.getFollowingCount() - 1));
            targetUser.setFollowersCount(Math.max(0, targetUser.getFollowersCount() - 1));

        } else {
            // FOLLOW
            follow = Follow.builder()
                    .follower(user)
                    .following(targetUser)
                    .createdAt(LocalDateTime.now())
                    .build();

            followRepository.save(follow);

            user.setFollowingCount(user.getFollowingCount() + 1);
            targetUser.setFollowersCount(targetUser.getFollowersCount() + 1);

            // SAFE NOTIFICATION
            try {
                notificationProducer.sendEvent(
                        "FOLLOW",
                        NotificationEvent.builder()
                                .userId(targetUser.getId())
                                .type("FOLLOW")
                                .message(user.getName() + " started following you")
                                .actorId(user.getId())
                                .build()
                );
            } catch (Exception ignored) {}
        }

        userRepository.save(user);
        userRepository.save(targetUser);
    }


    public List<FollowResponse> getFollowing(Long userId){

        List<Follow> follows = followRepository.findByFollowerId(userId);

        return follows.stream()
                .map(f -> FollowResponse.builder()
                        .userId(f.getFollowing().getId())
                        .name(f.getFollowing().getName())
                        .profileImage(f.getFollowing().getProfileImage())
                        .build())
                .toList();
    }

    public List<FollowResponse> getFollowers(Long userId){

        List<Follow> follows = followRepository.findByFollowingId(userId);

        return follows.stream()
                .map(f -> FollowResponse.builder()
                        .userId(f.getFollower().getId())
                        .name(f.getFollower().getName())
                        .profileImage(f.getFollower().getProfileImage())
                        .build())
                .toList();
    }
}
