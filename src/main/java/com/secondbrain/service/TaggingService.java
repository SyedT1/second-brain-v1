package com.secondbrain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaggingService {

    private final ChatClient chatClient;

    private static final String TAGGING_PROMPT = """
            Analyze the following content and provide:
            1. A list of 3-7 relevant tags (single words or short phrases)
            2. A concise summary (1-2 sentences)

            Title: {title}
            Content: {content}

            Return your response in the following format:
            TAGS: tag1, tag2, tag3
            SUMMARY: Your summary here
            """;

    public TaggingResult generateTagsAndSummary(String content, String title) {
        log.debug("Generating tags and summary for content length: {}", content.length());

        try {
            String titleValue = title != null ? title : "No title";
            String contentPreview = content.length() > 2000 ? content.substring(0, 2000) : content;

            String response = chatClient.prompt()
                    .user(userSpec -> userSpec.text(TAGGING_PROMPT)
                            .param("title", titleValue)
                            .param("content", contentPreview))
                    .call()
                    .content();

            return parseTaggingResponse(response);

        } catch (Exception e) {
            log.error("Error generating tags and summary", e);
            // Return defaults on error
            return new TaggingResult(
                    List.of("uncategorized"),
                    content.length() > 100 ? content.substring(0, 100) + "..." : content
            );
        }
    }

    private TaggingResult parseTaggingResponse(String response) {
        List<String> tags = List.of("uncategorized");
        String summary = "";

        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("TAGS:")) {
                    String tagsStr = line.substring(5).trim();
                    tags = Arrays.stream(tagsStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                } else if (line.startsWith("SUMMARY:")) {
                    summary = line.substring(8).trim();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing tagging response", e);
        }

        return new TaggingResult(tags, summary);
    }

    @Data
    @AllArgsConstructor
    public static class TaggingResult {
        private List<String> tags;
        private String summary;
    }
}
