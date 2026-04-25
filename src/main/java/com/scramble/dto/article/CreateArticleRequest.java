package com.scramble.dto.article;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

import java.util.List;

@Data
@Builder
public class CreateArticleRequest {
    private String title;
    private String content;
    private String subtitle;
    private String coverImage;
    private List<String> tags;
    private String status;
}