package com.aisalescrm.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;

    @Value("${app.gemini.model}")
    private String model;

    public GeminiService(@Qualifier("geminiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // ── Single-turn prompt ────────────────────────────────────────────────────

    public String sendPrompt(String systemInstruction, String userPrompt) {
        log.debug("Sending single-turn prompt to Gemini model: {}", model);
        Map<String, Object> requestBody = buildRequest(systemInstruction, userPrompt, null);
        return callGemini(requestBody);
    }

    // ── Multi-turn chat ───────────────────────────────────────────────────────

    public String sendChat(String systemInstruction,
                           List<Map<String, Object>> history,
                           String userMessage) {
        log.debug("Sending multi-turn chat to Gemini, history size: {}",
                history != null ? history.size() : 0);
        Map<String, Object> requestBody = buildRequest(systemInstruction, userMessage, history);
        return callGemini(requestBody);
    }

    // ── Request Builder ───────────────────────────────────────────────────────

    private Map<String, Object> buildRequest(String systemInstruction,
                                             String userPrompt,
                                             List<Map<String, Object>> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        // v1 API does not support systemInstruction field
        // Instead, prepend it as the first user message + model ack
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
            contents.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text", "Understood. I will follow these instructions."))
            ));
        }

        // Add conversation history
        if (history != null && !history.isEmpty()) {
            contents.addAll(history);
        }

        // Add current user message
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt))
        ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", contents);

        // DO NOT add systemInstruction field — not supported in v1
        // Only add generationConfig
        body.put("generationConfig", Map.of(
                "temperature", 0.7,
                "topK", 40,
                "topP", 0.95,
                "maxOutputTokens", 512
        ));

        return body;
    }

    // ── API Call ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callGemini(Map<String, Object> requestBody) {

        // Guard: API key missing

        String url = "/models/" + model + ":generateContent";
        log.debug("Calling Gemini URL: {}", url);


        try {
            log.info("Model = {}", model);
            log.info("URL = {}", url);
            log.info("Request = {}", requestBody);
            Map<?, ?> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("Gemini 4xx error: {}", body);
                                if (clientResponse.statusCode().value() == 400) {
                                    return new RuntimeException("Bad request to Gemini API: " + body);
                                } else if (clientResponse.statusCode().value() == 401 ||
                                        clientResponse.statusCode().value() == 403) {
                                    return new RuntimeException(
                                            "Invalid or unauthorized Gemini API key. Check your GEMINI_API_KEY.");
                                } else if (clientResponse.statusCode().value() == 429) {
                                    return new RuntimeException(
                                            "Gemini API rate limit reached. Please wait and try again.");
                                }
                                return new RuntimeException("Gemini API client error: " + body);
                            })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("FULL GEMINI ERROR = {}", body);
                                return new RuntimeException(body);
                            })
                    )
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(90))
                    .block();

            String result = extractText(response);
            log.debug("Gemini response received, length: {} chars", result.length());
            return result;

        } catch (WebClientResponseException e) {
            log.error("Gemini API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API error " + e.getStatusCode() + ": " + e.getMessage());
        } catch (RuntimeException e) {
            // Re-throw already-wrapped exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("AI service temporarily unavailable: " + e.getMessage());
        }
    }

    // ── Response Parser ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> response) {
        if (response == null) {
            log.warn("Gemini returned null response");
            return "No response from AI";
        }

        log.debug("Gemini raw response keys: {}", response.keySet());

        try {
            // Check for error field in response
            if (response.containsKey("error")) {
                Map<?, ?> error = (Map<?, ?>) response.get("error");
                String message = error.get("message") != null ? error.get("message").toString() : "Unknown error";
                log.error("Gemini returned error in body: {}", message);
                throw new RuntimeException("Gemini error: " + message);
            }

            List<?> candidates = (List<?>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("Gemini returned no candidates. Response: {}", response);
                return "No response generated. Please try again.";
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);

            // Check finish reason
            Object finishReason = candidate.get("finishReason");
            if ("SAFETY".equals(finishReason)) {
                log.warn("Gemini blocked response due to safety filters");
                return "Response was filtered by safety settings. Please rephrase your request.";
            }

            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            if (content == null) {
                log.warn("Gemini candidate has no content");
                return "Empty response from AI.";
            }

            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                log.warn("Gemini content has no parts");
                return "Empty response from AI.";
            }

            Map<?, ?> part = (Map<?, ?>) parts.get(0);
            Object text = part.get("text");

            if (text == null) {
                log.warn("Gemini part has no text field");
                return "No text in AI response.";
            }

            return text.toString().trim();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response, e);
            return "Failed to parse AI response. Please try again.";
        }
    }
}