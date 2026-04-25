package com.scramble.service;

import com.scramble.dto.Comment.CommentRequest;
import com.scramble.dto.Comment.CommentResponse;
import com.scramble.dto.Notification.NotificationEvent;
import com.scramble.entity.Article;
import com.scramble.entity.Comment;
import com.scramble.entity.User;
import com.scramble.kafka.NotificationProducer;
import com.scramble.repository.ArticleRepository;
import com.scramble.repository.CommentRepository;
import com.scramble.repository.UserRepository;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final NotificationProducer notificationProducer;

    public CommentResponse createComment(CommentRequest commentRequest){
        User user = securityUtils.getCurrentUser();

        Article article = articleRepository.findById(commentRequest.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article not found"));

        int newCommentsCount = article.getCommentCount()+1;
        article.setCommentCount(newCommentsCount);
        articleRepository.save(article);
        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .user(user)
                .article(article)
                .createdAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        User author = article.getAuthor();
        if (!author.getId().equals(user.getId())) {

            notificationProducer.sendEvent(
                    "COMMENT",
                    NotificationEvent.builder()
                            .userId(author.getId()) // receiver
                            .type("COMMENT")
                            .message(user.getName() + " commented on your article")
                            .articleId(article.getId())
                            .build()
            );
        }

        return mapToResponse(saved);
    }

    public List<CommentResponse>getCommentsByArticle(Long articleId){
        List<Comment>comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);

        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment){
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getUser().getName())
                .profileImage(
                        comment.getUser().getProfileImage() != null
                                ? comment.getUser().getProfileImage()
                                : "https://via.placeholder.com/100"
                )
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
