package com.scramble.dto.article;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticlePublishedEvent {

    private Long articleId;

    private String title;

    private String content;

    private Long authorId;
}