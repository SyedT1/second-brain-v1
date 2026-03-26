package com.secondbrain.service;

import com.secondbrain.event.EntryCreatedEvent;
import com.secondbrain.model.Entry;
import com.secondbrain.model.EntryType;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentService {

    private final EntryRepository entryRepository;
    private final TaggingService taggingService;
    private final EmbeddingService embeddingService;
    private final LinkEnricherService linkEnricherService;

    /**
     * Synchronous enrichment - called directly when creating an entry
     */
    @Transactional
    public Entry enrichEntry(Entry entry) {
        log.info("Starting synchronous enrichment for entry ID: {}", entry.getId());

        try {
            // Reload entry in this transaction
            Entry managedEntry = entryRepository.findById(entry.getId())
                    .orElseThrow(() -> new RuntimeException("Entry not found: " + entry.getId()));

            // Step 1: Generate tags and summary using LLM
            log.info("Generating tags for entry {}", managedEntry.getId());
            TaggingService.TaggingResult taggingResult = taggingService.generateTagsAndSummary(
                    managedEntry.getContent(),
                    managedEntry.getTitle()
            );
            managedEntry.setTags(taggingResult.getTags());
            managedEntry.setSummary(taggingResult.getSummary());

            // Step 2: Generate embeddings (SKIPPED FOR NOW - OpenAI credits needed)
            try {
                log.info("Generating embeddings for entry {}", managedEntry.getId());
                String textToEmbed = buildEmbeddingText(managedEntry);
                float[] embedding = embeddingService.generateEmbedding(textToEmbed);
                managedEntry.setEmbedding(embedding);
            } catch (Exception e) {
                log.warn("Skipping embeddings for entry {} - OpenAI not available: {}",
                        managedEntry.getId(), e.getMessage());
                // Continue without embeddings - they're optional for tagging to work
            }

            // Step 3: Enrich links if it's a LINK type
            if (managedEntry.getType() == EntryType.LINK && managedEntry.getUrl() != null) {
                log.info("Enriching link metadata for entry {}", managedEntry.getId());
                linkEnricherService.enrichLinkMetadata(managedEntry);
            }

            managedEntry.setEnrichedAt(LocalDateTime.now());
            entryRepository.save(managedEntry);

            log.info("Enrichment completed for entry ID: {}", managedEntry.getId());

            return managedEntry;

        } catch (Exception e) {
            log.error("Error enriching entry ID: {}", entry.getId(), e);
            // Return the entry as-is if enrichment fails
            return entry;
        }
    }

    /**
     * Async enrichment - kept for backward compatibility (currently unused)
     */
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleEntryCreated(EntryCreatedEvent event) {
        Entry entry = event.getEntry();
        enrichEntry(entry);
    }

    private String buildEmbeddingText(Entry entry) {
        StringBuilder text = new StringBuilder();

        if (entry.getTitle() != null) {
            text.append(entry.getTitle()).append(" ");
        }

        text.append(entry.getContent());

        if (entry.getSummary() != null) {
            text.append(" ").append(entry.getSummary());
        }

        if (entry.getTags() != null && !entry.getTags().isEmpty()) {
            text.append(" Tags: ").append(String.join(", ", entry.getTags()));
        }

        return text.toString().trim();
    }
}
