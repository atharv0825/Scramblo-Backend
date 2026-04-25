package com.scramble.service;

import com.scramble.ai.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AISummaryService {

    private final GeminiClient geminiClient;

    public String generateSummary(String content) {
        return geminiClient.generateSummary(content);
    }

}