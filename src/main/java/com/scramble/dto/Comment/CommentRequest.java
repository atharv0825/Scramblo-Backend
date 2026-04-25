package com.scramble.dto.Comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentRequest {
    private String content;
    private Long articleId;
}
