package com.scramble.dto.Follow;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {
    private Long userId;
    private String name;
    private String profileImage;
}