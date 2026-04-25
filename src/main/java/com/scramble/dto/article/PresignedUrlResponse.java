package com.scramble.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PresignedUrlResponse {
    private String uploadUrl;
    private String fileUrl; // final S3 URL
}