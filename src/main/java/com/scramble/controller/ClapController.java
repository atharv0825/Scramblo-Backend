package com.scramble.controller;

import com.scramble.service.ClapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class ClapController {

    private final ClapService clapService;

    @PostMapping("/{articleId}")
    public ResponseEntity<Map<String, Object>> clap(@PathVariable Long articleId) {

        boolean liked = clapService.toggleClap(articleId);

        return ResponseEntity.ok(       Map.of(
                "liked", liked
        ));
    }
}