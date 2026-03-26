package com.secondbrain.service;

import com.secondbrain.dto.EntryResponse;
import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightsService {

    private final EntryRepository entryRepository;

    /**
     * Knowledge Resurfacing: Find entries not viewed in X days
     */
    @Transactional(readOnly = true)
    public List<EntryResponse> getResurfacedEntries(int daysAgo) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAgo);

        log.info("Finding entries older than {} days (before {})", daysAgo, cutoffDate);

        List<Entry> oldEntries = entryRepository.findByCreatedAtBeforeOrderByCreatedAtAsc(cutoffDate);

        // Limit to 10 most forgotten entries
        return oldEntries.stream()
                .limit(10)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get forgotten knowledge categorized by age
     */
    @Transactional(readOnly = true)
    public ForgottenKnowledge getForgottenKnowledge() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

        List<Entry> entries30 = entryRepository.findByCreatedAtBeforeOrderByCreatedAtAsc(thirtyDaysAgo);
        List<Entry> entries60 = entryRepository.findByCreatedAtBeforeOrderByCreatedAtAsc(sixtyDaysAgo);
        List<Entry> entries90 = entryRepository.findByCreatedAtBeforeOrderByCreatedAtAsc(ninetyDaysAgo);

        return ForgottenKnowledge.builder()
                .thirtyDaysAgo(entries30.stream().limit(5).map(this::toResponse).collect(Collectors.toList()))
                .sixtyDaysAgo(entries60.stream().limit(5).map(this::toResponse).collect(Collectors.toList()))
                .ninetyDaysAgo(entries90.stream().limit(5).map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    private EntryResponse toResponse(Entry entry) {
        return EntryResponse.builder()
                .id(entry.getId())
                .type(entry.getType())
                .content(entry.getContent())
                .title(entry.getTitle())
                .url(entry.getUrl())
                .tags(entry.getTags())
                .summary(entry.getSummary())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .enrichedAt(entry.getEnrichedAt())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class ForgottenKnowledge {
        private List<EntryResponse> thirtyDaysAgo;
        private List<EntryResponse> sixtyDaysAgo;
        private List<EntryResponse> ninetyDaysAgo;
    }
}
