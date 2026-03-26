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
public class ContradictionDetectorService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    /**
     * Detect contradictions in a new entry against existing knowledge base
     */
    @Transactional(readOnly = true)
    public ContradictionAnalysis detectContradictions(Long entryId) {
        log.info("Detecting contradictions for entry {}", entryId);

        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found: " + entryId));

        try {
            List<Entry> allEntries = entryRepository.findAll().stream()
                    .filter(e -> !e.getId().equals(entryId)) // Exclude the entry itself
                    .collect(Collectors.toList());

            // Find potentially conflicting entries
            List<Entry> potentialConflicts = findPotentialConflicts(entry, allEntries);

            if (potentialConflicts.isEmpty()) {
                return ContradictionAnalysis.builder()
                        .entryId(entryId)
                        .contradictions(List.of())
                        .isSafe(true)
                        .build();
            }

            // Use LLM to analyze contradictions
            String analysis = analyzeContradictions(entry, potentialConflicts);

            List<String> contradictions = extractContradictions(analysis, potentialConflicts);

            return ContradictionAnalysis.builder()
                    .entryId(entryId)
                    .contradictions(contradictions)
                    .analysis(analysis)
                    .conflictingEntries(potentialConflicts.stream()
                            .map(Entry::getTitle)
                            .collect(Collectors.toList()))
                    .isSafe(contradictions.isEmpty())
                    .build();

        } catch (Exception e) {
            log.error("Error detecting contradictions", e);
            return ContradictionAnalysis.builder()
                    .entryId(entryId)
                    .contradictions(List.of())
                    .isSafe(true)
                    .build();
        }
    }

    /**
     * Find entries that might conflict with the given entry
     */
    private List<Entry> findPotentialConflicts(Entry entry, List<Entry> candidates) {
        Set<String> keywords = new HashSet<>();

        // Extract keywords from title and tags
        Arrays.stream(entry.getTitle().split("\\s+"))
                .filter(w -> w.length() > 4)
                .forEach(keywords::add);

        if (entry.getTags() != null) {
            keywords.addAll(entry.getTags());
        }

        // Find entries with matching keywords
        return candidates.stream()
                .filter(e -> keywords.stream().anyMatch(k ->
                        e.getTitle().toLowerCase().contains(k.toLowerCase()) ||
                        (e.getTags() != null && e.getTags().stream()
                            .anyMatch(t -> t.toLowerCase().contains(k.toLowerCase())))))
                .limit(5) // Limit to top 5 to avoid LLM overload
                .collect(Collectors.toList());
    }

    /**
     * Use LLM to analyze if entries contradict
     */
    private String analyzeContradictions(Entry newEntry, List<Entry> potentialConflicts) {
        String conflictSummaries = potentialConflicts.stream()
                .map(e -> "- " + e.getTitle() + ": " + e.getSummary())
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            Analyze if this NEW entry contradicts any existing entries in knowledge base.

            NEW ENTRY:
            Title: %s
            Summary: %s
            Tags: %s

            POTENTIALLY RELATED EXISTING ENTRIES:
            %s

            Please check for:
            1. Direct contradictions (conflicting facts/claims)
            2. Logical inconsistencies
            3. Opposite conclusions
            4. Conflicting methodologies or approaches

            Format response as:
            - Contradiction 1: [specific conflict]
            - Contradiction 2: [specific conflict]
            OR state "No contradictions found" if safe.

            Be precise about what conflicts and why.
            """,
            newEntry.getTitle(),
            newEntry.getSummary() != null ? newEntry.getSummary() : newEntry.getContent(),
            newEntry.getTags() != null ? String.join(", ", newEntry.getTags()) : "None",
            conflictSummaries
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * Extract contradiction list from LLM analysis
     */
    private List<String> extractContradictions(String analysis, List<Entry> conflicts) {
        List<String> contradictions = new ArrayList<>();

        String[] lines = analysis.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("-") && line.toLowerCase().contains("contradict")) {
                contradictions.add(line.trim().substring(1).trim());
            }
        }

        return contradictions;
    }

    // DTO
    @lombok.Data
    @lombok.Builder
    public static class ContradictionAnalysis {
        private Long entryId;
        private List<String> contradictions;
        private String analysis;
        private List<String> conflictingEntries;
        private boolean isSafe;
    }
}
