package com.scramble.controller;

import com.scramble.dto.Comment.CommentRequest;
import com.scramble.dto.Comment.CommentResponse;
import com.scramble.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse>createComment(@RequestBody CommentRequest commentRequest){
        CommentResponse response = commentService.createComment(commentRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<List<CommentResponse>>getComments(@PathVariable Long articleId){
        List<CommentResponse>comments = commentService.getCommentsByArticle(articleId);
        return ResponseEntity.ok(comments);
    }
}
