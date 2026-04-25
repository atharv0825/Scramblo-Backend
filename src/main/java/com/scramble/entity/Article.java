package com.scramble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Table(name = "articles")
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String subtitle;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @Column(name = "preview_token")
    private String previewToken;
    @Enumerated(EnumType.STRING)
    private SummaryStatus summaryStatus;

    private int readingTime;
    private int commentCount = 0;
    private int clapCount = 0;
    private long viewCount = 0;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private int retryCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

}
