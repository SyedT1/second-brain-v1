package com.secondbrain.service;

import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingAssistantService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    /**
     * Get contextual suggestions while writing
     * Analyzes partial text and suggests relevant KB entries
     */
    @Transactional(readOnly = true)
    public WritingSuggestions getSuggestions(String partialText) {
        log.info("Getting writing suggestions for: {}", partialText.substring(0, Math.min(50, partialText.length())));

        try {
            List<Entry> allEntries = entryRepository.findAll();

            // Find potentially relevant entries
            List<Entry> relevantEntries = findRelevantEntries(partialText, allEntries);

            // Get AI suggestions
            String suggestedText = generateSuggestions(partialText, relevantEntries);

            return WritingSuggestions.builder()
                    .originalText(partialText)
                    .suggestedContinuation(suggestedText)
                    .relevantEntries(relevantEntries.stream()
                            .map(e -> RelevantEntry.builder()
                                    .id(e.getId())
                                    .title(e.getTitle())
                                    .excerpt(e.getSummary() != null ? e.getSummary() : e.getContent().substring(0, Math.min(100, e.getContent().length())))
                                    .tags(e.getTags() != null ? e.getTags() : List.of())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

        } catch (Exception e) {
            log.error("Error generating writing suggestions", e);
            return WritingSuggestions.builder()
                    .originalText(partialText)
                    .suggestedContinuation("")
                    .relevantEntries(List.of())
                    .build();
        }
    }

    /**
     * Find entries relevant to the writing context
     */
    private List<Entry> findRelevantEntries(String text, List<Entry> allEntries) {
        String[] words = text.toLowerCase().split("\\s+");
        Set<String> keywords = new HashSet<>(Arrays.asList(words));

        return allEntries.stream()
                .map(e -> {
                    int relevanceScore = 0;

                    // Score based on keyword match
                    for (String keyword : keywords) {
                        if (keyword.length() > 3) { // Skip short words
                            if (e.getTitle().toLowerCase().contains(keyword)) relevanceScore += 3;
                            if (e.getContent().toLowerCase().contains(keyword)) relevanceScore += 2;
                            if (e.getTags() != null && e.getTags().stream()
                                    .anyMatch(t -> t.toLowerCase().contains(keyword))) relevanceScore += 4;
                        }
                    }

                    return new AbstractMap.SimpleEntry<>(e, relevanceScore);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Entry, Integer>comparingByValue().reversed())
                .limit(5)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Generate writing suggestions using LLM
     */
    private String generateSuggestions(String partialText, List<Entry> relevantEntries) {
        String context = relevantEntries.stream()
                .map(e -> "- " + e.getTitle() + ": " + e.getSummary())
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            You are a writing assistant. Help complete this text based on relevant knowledge base entries.

            CURRENT TEXT:
            "%s"

            RELEVANT KNOWLEDGE BASE ENTRIES:
            %s

            Please provide:
            1. A natural continuation (2-3 sentences) that builds on the current text
            2. Suggestions for related topics to mention
            3. Relevant facts or points from the KB entries above

            Keep the writing style consistent and professional.
            Do not just repeat the KB entries - synthesize them into natural prose.
            """,
            partialText,
            context.isEmpty() ? "No relevant entries found" : context
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    // DTOs
    @lombok.Data
    @lombok.Builder
    public static class WritingSuggestions {
        private String originalText;
        private String suggestedContinuation;
        private List<RelevantEntry> relevantEntries;
    }

    @lombok.Data
    @lombok.Builder
    public static class RelevantEntry {
        private Long id;
        private String title;
        private String excerpt;
        private List<String> tags;
    }
}
