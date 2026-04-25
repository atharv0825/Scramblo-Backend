package com.scramble.controller;

import com.scramble.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{articleId}")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long articleId
    ) {

        boolean bookmarked = bookmarkService.toggleBookmark(articleId);

        return ResponseEntity.ok(
                Map.of("bookmarked", bookmarked)
        );
    }
}

