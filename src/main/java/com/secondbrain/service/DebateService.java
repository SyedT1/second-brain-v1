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
public class DebateService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    /**
     * Stress-test a claim by finding counterarguments from your knowledge base
     */
    @Transactional(readOnly = true)
    public StressTestResult stressTestClaim(String claim) {
        log.info("Stress-testing claim: {}", claim);

        try {
            // Get all entries from KB
            List<Entry> allEntries = entryRepository.findAll();

            // Build context from entries
            String knowledgeBase = allEntries.stream()
                    .map(e -> e.getTitle() + ": " + e.getSummary())
                    .limit(10)
                    .collect(Collectors.joining("\n"));

            // Use LLM to find counterarguments
            String prompt = String.format("""
                You are a critical thinking advisor. Analyze this claim and find counterarguments.

                CLAIM TO TEST:
                "%s"

                KNOWLEDGE BASE CONTEXT (from user's saved entries):
                %s

                Please provide:
                1. Strengths of the claim (2-3 points)
                2. Weaknesses/Counterarguments (3-4 points)
                3. Missing context or assumptions (2-3 points)
                4. Balanced conclusion (1-2 sentences)
                5. Questions to further validate (3 thought-provoking questions)

                Be rigorous and devil's advocate. Consider edge cases and alternative perspectives.
                """,
                claim,
                knowledgeBase.substring(0, Math.min(1500, knowledgeBase.length()))
            );

            String analysis = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return StressTestResult.builder()
                    .claim(claim)
                    .analysis(analysis)
                    .relatedEntries(findRelatedEntries(claim))
                    .build();

        } catch (Exception e) {
            log.error("Error stress-testing claim", e);
            return StressTestResult.builder()
                    .claim(claim)
                    .analysis("Error: " + e.getMessage())
                    .relatedEntries(List.of())
                    .build();
        }
    }

    /**
     * Find entries related to the claim
     */
    private List<String> findRelatedEntries(String claim) {
        List<Entry> allEntries = entryRepository.findAll();
        return allEntries.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(claim.toLowerCase()) ||
                           e.getContent().toLowerCase().contains(claim.toLowerCase()) ||
                           (e.getTags() != null && e.getTags().stream()
                               .anyMatch(t -> t.toLowerCase().contains(claim.toLowerCase()))))
                .map(Entry::getTitle)
                .collect(Collectors.toList());
    }

    // DTO
    @lombok.Data
    @lombok.Builder
    public static class StressTestResult {
        private String claim;
        private String analysis;
        private List<String> relatedEntries;
    }
}
