package com.scramble.dto.User;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private long id;
    private String name;
    private String bio;
    private String profileImage;
    private int followersCount;
    private int followingCount;
    private boolean profileCompleted;
    private String coverImage;
    private String instagram;
    private String twitter;
    private String linkedin;
    private List<String>tags;
    private LocalDateTime createdAt;
}
