package com.supportai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-3.8-flash}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String generateContent(String systemInstruction, String userPrompt) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String fullPrompt = (systemInstruction != null && !systemInstruction.isBlank())
                ? systemInstruction + "\n\nUser: " + userPrompt
                : userPrompt;

        String[] modelsToTry = {model, "gemini-3.7-flash", "gemini-3.6-flash", "gemini-2.5-flash"};

        Exception lastException = null;

        for (String candidateModel : modelsToTry) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    return callModel(candidateModel, fullPrompt);
                } catch (Exception e) {
                    lastException = e;
                    logger.warn("Gemini model {} attempt {}/2 failed: {}", candidateModel, attempt, e.getMessage());

                    if (attempt < 2 && isRetryable(e)) {
                        Thread.sleep(1500L * attempt);
                    } else {
                        break;
                    }
                }
            }

            logger.warn("Gemini model {} unavailable; trying fallback model.", candidateModel);
        }

        throw lastException != null
                ? lastException
                : new RuntimeException("No Gemini model could generate a response.");
    }

    private String callModel(String candidateModel, String fullPrompt) throws Exception {
        Map<String, Object> part = Map.of("text", fullPrompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + candidateModel + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    if (text != null && !text.isBlank()) {
                        return text;
                    }
                }
            }

            throw new RuntimeException("Unexpected response format from Gemini.");
        }

        logger.warn("Gemini API error from {}: HTTP {} - {}",
                candidateModel, response.statusCode(), response.body());

        throw new RuntimeException(
                "Gemini API error: HTTP " + response.statusCode()
        );
    }

    private boolean isRetryable(Exception e) {
        String message = e.getMessage();
        return message != null &&
                (message.contains("HTTP 429") ||
                 message.contains("HTTP 500") ||
                 message.contains("HTTP 502") ||
                 message.contains("HTTP 503") ||
                 message.contains("HTTP 504"));
    }
}
