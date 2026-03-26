package com.secondbrain.service;

import com.secondbrain.dto.EntryResponse;
import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyBriefService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    /**
     * Generate weekly intelligence brief
     * Scheduled to run every Monday at 9 AM (cron: seconds minutes hours day month day-of-week)
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyBrief() {
        log.info("Generating weekly intelligence brief");
        try {
            WeeklyBrief brief = generateBriefForCurrentWeek();
            log.info("Weekly brief generated successfully: {}", brief.getSummary().substring(0, 100));
        } catch (Exception e) {
            log.error("Error generating weekly brief", e);
        }
    }

    /**
     * Generate brief for current week (Monday to Sunday)
     */
    @Transactional(readOnly = true)
    public WeeklyBrief generateBriefForCurrentWeek() {
        LocalDateTime monday = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime sunday = monday.plusDays(7);

        List<Entry> weekEntries = entryRepository.findAll().stream()
                .filter(e -> e.getCreatedAt().isAfter(monday) && e.getCreatedAt().isBefore(sunday))
                .collect(Collectors.toList());

        return generateBriefFromEntries(weekEntries);
    }

    /**
     * Generate brief from list of entries
     */
    private WeeklyBrief generateBriefFromEntries(List<Entry> entries) {
        if (entries.isEmpty()) {
            return WeeklyBrief.builder()
                    .summary("No entries saved this week.")
                    .themes(List.of())
                    .suggestedActions(List.of())
                    .entriesCount(0)
                    .build();
        }

        // Aggregate tags to find themes
        Map<String, Integer> tagFrequency = new HashMap<>();
        StringBuilder contentSummary = new StringBuilder();

        for (Entry entry : entries) {
            if (entry.getTags() != null) {
                for (String tag : entry.getTags()) {
                    tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                }
            }
            contentSummary.append(entry.getTitle()).append(": ").append(entry.getSummary()).append("\n");
        }

        // Get top themes
        List<String> topThemes = tagFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Use LLM to generate brief
        String prompt = String.format("""
            Generate a personalized weekly intelligence brief based on this week's knowledge base entries:

            Entries saved this week (%d total):
            %s

            Top themes: %s

            Please provide:
            1. Executive Summary (2-3 sentences)
            2. Emerging Patterns (3-4 bullet points)
            3. Knowledge Gaps Observed (2-3 areas not covered)
            4. Suggested Actions (3-4 next steps for learning)
            5. Interesting Connections (relationships between topics)

            Format as clear sections with bullet points.
            """,
            entries.size(),
            contentSummary.substring(0, Math.min(2000, contentSummary.length())),
            String.join(", ", topThemes)
        );

        try {
            String analysis = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            List<String> suggestedActions = extractSuggestedActions(analysis);

            return WeeklyBrief.builder()
                    .summary(analysis)
                    .themes(topThemes)
                    .suggestedActions(suggestedActions)
                    .entriesCount(entries.size())
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error generating brief with LLM", e);
            return WeeklyBrief.builder()
                    .summary("Error generating brief: " + e.getMessage())
                    .themes(topThemes)
                    .suggestedActions(List.of())
                    .entriesCount(entries.size())
                    .build();
        }
    }

    private List<String> extractSuggestedActions(String analysis) {
        List<String> actions = new ArrayList<>();
        String[] lines = analysis.split("\n");
        boolean inActionsSection = false;

        for (String line : lines) {
            if (line.contains("Suggested Actions") || line.contains("Next Steps")) {
                inActionsSection = true;
                continue;
            }
            if (inActionsSection && line.trim().startsWith("-")) {
                actions.add(line.trim().substring(1).trim());
                if (actions.size() >= 3) break;
            }
        }

        return actions;
    }

    // DTO
    @lombok.Data
    @lombok.Builder
    public static class WeeklyBrief {
        private String summary;
        private List<String> themes;
        private List<String> suggestedActions;
        private int entriesCount;
        private LocalDateTime generatedAt;
    }
}
