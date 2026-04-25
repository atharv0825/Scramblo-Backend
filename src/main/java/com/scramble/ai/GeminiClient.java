package com.scramble.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiClient {

    private final Client client;

    // 🔒 Global lock (prevents parallel calls)
    private static final Object lock = new Object();

    public GeminiClient(@Value("${gemini.api-key}") String apiKey) {
        this.client = new Client.Builder()
                .apiKey(apiKey)
                .build();
    }

    public String generateSummary(String content) {

        String plainText = Jsoup.parse(content).text();
        String prompt = "Summarize this article in 3-4 concise lines:\n" + plainText;

        int retries = 3;

        for (int i = 0; i < retries; i++) {

            try {
                synchronized (lock) {

                    Thread.sleep(3000);

                    GenerateContentResponse response =
                            client.models.generateContent(
                                    "gemini-2.5-flash-lite",
                                    prompt,
                                    null
                            );

                    return response.text();
                }

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    try {
                        System.out.println("Rate limit hit. Retrying...");
                        Thread.sleep(20000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new RuntimeException("Failed to generate summary after retries");
    }
}