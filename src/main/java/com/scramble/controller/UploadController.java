package com.scramble.controller;

import com.scramble.dto.article.PresignedUrlRequest;
import com.scramble.dto.article.PresignedUrlResponse;
import com.scramble.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3Service s3Service;

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {

        PresignedUrlResponse response = s3Service.generatePresignedUrl(
                request.getFileName(),
                request.getContentType(),
                request.getFolder()
        );

        return ResponseEntity.ok(response);
    }
}