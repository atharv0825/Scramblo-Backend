package com.scramble.dto.article;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PresignedUrlRequest {
    private String fileName;
    private String folder;
    private String contentType;
}