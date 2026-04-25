package com.scramble.controller;

import com.scramble.dto.Follow.FollowResponse;
import com.scramble.repository.FollowRepository;
import com.scramble.security.SecurityUtils;
import com.scramble.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final SecurityUtils securityUtils;
    private final FollowRepository followRepository;

    @PostMapping("/{userId}")
    public ResponseEntity<String> follow(@PathVariable Long userId) {
        followService.follow(userId);
        return ResponseEntity.ok("Follow action completed");
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/is-following/{userId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followRepository
                .existsByFollowerIdAndFollowingId(
                        securityUtils.getCurrentUser().getId(), userId
                ));
    }
}