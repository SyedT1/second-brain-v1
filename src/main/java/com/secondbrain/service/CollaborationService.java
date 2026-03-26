package com.secondbrain.service;

import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {

    private final EntryRepository entryRepository;

    /**
     * Simulate collaborative merge between two topic areas
     * In production, this would work with multi-user system
     */
    @Transactional(readOnly = true)
    public CollaborationReport generateCollaborationReport(String topic1, String topic2) {
        log.info("Generating collaboration report between {} and {}", topic1, topic2);

        List<Entry> allEntries = entryRepository.findAll();

        // Find entries matching each topic
        List<Entry> topic1Entries = findEntriesByTopic(topic1, allEntries);
        List<Entry> topic2Entries = findEntriesByTopic(topic2, allEntries);

        // Find overlaps
        Set<String> tags1 = extractAllTags(topic1Entries);
        Set<String> tags2 = extractAllTags(topic2Entries);

        Set<String> overlappingTags = new HashSet<>(tags1);
        overlappingTags.retainAll(tags2);

        Set<String> uniqueToFirst = new HashSet<>(tags1);
        uniqueToFirst.removeAll(tags2);

        Set<String> uniqueToSecond = new HashSet<>(tags2);
        uniqueToSecond.removeAll(tags1);

        // Find contradictions
        List<String> potentialConflicts = findPotentialConflicts(topic1Entries, topic2Entries);

        return CollaborationReport.builder()
                .topic1(topic1)
                .topic2(topic2)
                .topic1Count(topic1Entries.size())
                .topic2Count(topic2Entries.size())
                .overlapCount(topic1Entries.stream()
                        .filter(e2 -> topic2Entries.stream().anyMatch(e1 ->
                                e1.getTags() != null && e2.getTags() != null &&
                                e1.getTags().stream().anyMatch(e2.getTags()::contains)))
                        .count())
                .sharedTags(new ArrayList<>(overlappingTags))
                .uniqueToTopic1(new ArrayList<>(uniqueToFirst))
                .uniqueToTopic2(new ArrayList<>(uniqueToSecond))
                .potentialConflicts(potentialConflicts)
                .collaborationOpportunities(generateOpportunities(topic1, topic2, overlappingTags))
                .build();
    }

    /**
     * Find entries by topic/tag
     */
    private List<Entry> findEntriesByTopic(String topic, List<Entry> entries) {
        return entries.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(topic.toLowerCase()) ||
                           e.getContent().toLowerCase().contains(topic.toLowerCase()) ||
                           (e.getTags() != null && e.getTags().stream()
                               .anyMatch(t -> t.toLowerCase().contains(topic.toLowerCase()))))
                .collect(Collectors.toList());
    }

    /**
     * Extract all tags from entries
     */
    private Set<String> extractAllTags(List<Entry> entries) {
        return entries.stream()
                .filter(e -> e.getTags() != null)
                .flatMap(e -> e.getTags().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Find potential conflicts between entries
     */
    private List<String> findPotentialConflicts(List<Entry> topic1Entries, List<Entry> topic2Entries) {
        List<String> conflicts = new ArrayList<>();

        for (Entry e1 : topic1Entries) {
            for (Entry e2 : topic2Entries) {
                if (e1.getTags() != null && e2.getTags() != null) {
                    Set<String> commonTags = new HashSet<>(e1.getTags());
                    commonTags.retainAll(e2.getTags());

                    // If they share tags but have different perspectives
                    if (!commonTags.isEmpty() && !e1.getSummary().equals(e2.getSummary())) {
                        conflicts.add("Potential conflict: " + e1.getTitle() + " vs " + e2.getTitle());
                    }
                }
            }
        }

        return conflicts.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * Generate collaboration opportunities
     */
    private List<String> generateOpportunities(String topic1, String topic2, Set<String> sharedTags) {
        List<String> opportunities = new ArrayList<>();

        if (!sharedTags.isEmpty()) {
            opportunities.add("Strong intersection on: " + String.join(", ", sharedTags));
        }

        opportunities.add("Investigate how " + topic1 + " and " + topic2 + " connect");
        opportunities.add("Create a combined knowledge base on shared topics");
        opportunities.add("Exchange perspectives on areas of expertise difference");

        return opportunities;
    }

    // DTO
    @lombok.Data
    @lombok.Builder
    public static class CollaborationReport {
        private String topic1;
        private String topic2;
        private int topic1Count;
        private int topic2Count;
        private long overlapCount;
        private List<String> sharedTags;
        private List<String> uniqueToTopic1;
        private List<String> uniqueToTopic2;
        private List<String> potentialConflicts;
        private List<String> collaborationOpportunities;
    }
}
