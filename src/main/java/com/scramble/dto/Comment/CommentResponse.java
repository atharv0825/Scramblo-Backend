package com.scramble.dto.Comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String username;
    private String profileImage;
    private LocalDateTime createdAt;
}
