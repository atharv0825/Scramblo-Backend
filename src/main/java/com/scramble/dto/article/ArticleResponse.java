package com.scramble.dto.article;

import com.scramble.dto.User.UserResponse;
import com.scramble.entity.ArticleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Long id;
    private String summaryStatus;
    private ArticleStatus status;
    private String title;
    private String coverImage;
    private String content;
    private String summary;
    private String subtitle;
    private int readingTime;
    private Long viewCount;
    private int commentCount;
    private int clapCount;
    private boolean isLiked;
    private boolean bookmarked;
    private UserResponse author;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
