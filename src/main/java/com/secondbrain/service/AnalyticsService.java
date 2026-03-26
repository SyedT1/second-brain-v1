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
public class AnalyticsService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    /**
     * Expertise Tracker: Calculate expertise level by topic/tag
     */
    @Transactional(readOnly = true)
    public ExpertiseReport getExpertiseReport() {
        log.info("Generating expertise report");

        List<Entry> allEntries = entryRepository.findAll();

        // Count entries by tag
        Map<String, Integer> tagCounts = new HashMap<>();
        for (Entry entry : allEntries) {
            if (entry.getTags() != null) {
                for (String tag : entry.getTags()) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            }
        }

        // Convert to expertise scores (more entries = higher expertise)
        List<ExpertiseScore> expertiseScores = tagCounts.entrySet().stream()
                .map(e -> new ExpertiseScore(
                        e.getKey(),
                        e.getValue(),
                        calculateExpertiseLevel(e.getValue())
                ))
                .sorted(Comparator.comparingInt(ExpertiseScore::getEntryCount).reversed())
                .limit(20) // Top 20 topics
                .collect(Collectors.toList());

        // Calculate overall stats
        int totalEntries = allEntries.size();
        int totalTags = tagCounts.size();
        int expertTopics = (int) expertiseScores.stream()
                .filter(s -> s.getLevel() == ExpertiseLevel.EXPERT)
                .count();

        return ExpertiseReport.builder()
                .expertiseScores(expertiseScores)
                .totalEntries(totalEntries)
                .totalTags(totalTags)
                .expertTopics(expertTopics)
                .build();
    }

    /**
     * Knowledge Gaps Detector: Identify missing knowledge areas
     */
    @Transactional(readOnly = true)
    public KnowledgeGapsReport detectKnowledgeGaps() {
        log.info("Detecting knowledge gaps");

        List<Entry> allEntries = entryRepository.findAll();

        // Collect all tags
        Map<String, Integer> tagFrequency = new HashMap<>();
        StringBuilder allContent = new StringBuilder();

        for (Entry entry : allEntries) {
            if (entry.getTags() != null) {
                for (String tag : entry.getTags()) {
                    tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                }
            }
            if (entry.getSummary() != null) {
                allContent.append(entry.getSummary()).append(". ");
            }
        }

        // Use LLM to analyze knowledge gaps
        String prompt = String.format("""
            Analyze this person's knowledge base and identify gaps or missing areas they should explore:

            Topics covered (with frequency):
            %s

            Sample content summaries:
            %s

            Instructions:
            - Identify 5-7 knowledge gaps or related areas not yet explored
            - For each gap, explain why it's relevant given their existing knowledge
            - Suggest 2-3 specific resources (books, courses, articles) to fill each gap
            - Format as numbered list with explanations

            Focus on logical extensions and complementary knowledge areas.
            """,
            tagFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20)
                    .map(e -> e.getKey() + " (" + e.getValue() + " entries)")
                    .collect(Collectors.joining(", ")),
            allContent.substring(0, Math.min(2000, allContent.length()))
        );

        try {
            String gapsAnalysis = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return KnowledgeGapsReport.builder()
                    .analysis(gapsAnalysis)
                    .topicsCovered(tagFrequency.keySet().stream().sorted().collect(Collectors.toList()))
                    .totalTopics(tagFrequency.size())
                    .build();

        } catch (Exception e) {
            log.error("Error detecting knowledge gaps", e);
            return KnowledgeGapsReport.builder()
                    .analysis("Error analyzing knowledge gaps: " + e.getMessage())
                    .topicsCovered(new ArrayList<>())
                    .totalTopics(0)
                    .build();
        }
    }

    private ExpertiseLevel calculateExpertiseLevel(int entryCount) {
        if (entryCount >= 10) return ExpertiseLevel.EXPERT;
        if (entryCount >= 5) return ExpertiseLevel.INTERMEDIATE;
        if (entryCount >= 2) return ExpertiseLevel.BEGINNER;
        return ExpertiseLevel.NOVICE;
    }

    // DTOs
    @lombok.Data
    @lombok.Builder
    public static class ExpertiseReport {
        private List<ExpertiseScore> expertiseScores;
        private int totalEntries;
        private int totalTags;
        private int expertTopics;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ExpertiseScore {
        private String topic;
        private int entryCount;
        private ExpertiseLevel level;

        public String getLevelName() {
            return level.name();
        }
    }

    public enum ExpertiseLevel {
        NOVICE,      // 1 entry
        BEGINNER,    // 2-4 entries
        INTERMEDIATE,// 5-9 entries
        EXPERT       // 10+ entries
    }

    @lombok.Data
    @lombok.Builder
    public static class KnowledgeGapsReport {
        private String analysis;
        private List<String> topicsCovered;
        private int totalTopics;
    }
}
