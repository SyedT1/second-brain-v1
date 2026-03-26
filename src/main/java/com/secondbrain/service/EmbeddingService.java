package com.secondbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean isEnabled;

    public EmbeddingService(@Value("${openai.embedding.api-key:}") String apiKey) {
        this.objectMapper = new ObjectMapper();

        // Check if OpenAI API key is provided
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("OpenAI API key not configured - embeddings will be disabled");
            this.isEnabled = false;
            this.restClient = null;
        } else {
            this.isEnabled = true;
            this.restClient = RestClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            log.info("OpenAI embeddings enabled");
        }
    }

    public float[] generateEmbedding(String text) {
        if (!isEnabled) {
            log.debug("Embeddings disabled - skipping");
            throw new RuntimeException("OpenAI API key not configured - embeddings disabled");
        }

        log.debug("Generating embedding for text length: {}", text.length());

        try {
            // Prepare request body
            Map<String, Object> requestBody = Map.of(
                    "input", text,
                    "model", "text-embedding-3-small"
            );

            // Call OpenAI API directly
            String response = restClient.post()
                    .uri("/embeddings")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode embeddingArray = jsonNode
                    .get("data")
                    .get(0)
                    .get("embedding");

            // Convert to float array
            float[] result = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                result[i] = (float) embeddingArray.get(i).asDouble();
            }

            log.debug("Generated embedding with dimension: {}", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    public float[] generateEmbeddingFromTexts(List<String> texts) {
        String combined = String.join(" ", texts);
        return generateEmbedding(combined);
    }
}
