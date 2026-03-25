package com.secondbrain.service;

import com.secondbrain.dto.CreateEntryRequest;
import com.secondbrain.dto.EntryResponse;
import com.secondbrain.event.EntryCreatedEvent;
import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryService {

    private final EntryRepository entryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EntryResponse createEntry(CreateEntryRequest request) {
        log.info("Creating new entry of type: {}", request.getType());

        Entry entry = Entry.builder()
                .type(request.getType())
                .content(request.getContent())
                .title(request.getTitle())
                .url(request.getUrl())
                .filePath(request.getFilePath())
                .metadata(request.getMetadata())
                .build();

        Entry savedEntry = entryRepository.save(entry);
        log.info("Entry created with ID: {}", savedEntry.getId());

        // Publish event for async enrichment
        eventPublisher.publishEvent(new EntryCreatedEvent(this, savedEntry));

        return toResponse(savedEntry);
    }

    @Transactional(readOnly = true)
    public EntryResponse getEntry(Long id) {
        Entry entry = entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found: " + id));
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<EntryResponse> getAllEntries() {
        return entryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEntry(Long id) {
        log.info("Deleting entry: {}", id);
        entryRepository.deleteById(id);
    }

    private EntryResponse toResponse(Entry entry) {
        return EntryResponse.builder()
                .id(entry.getId())
                .type(entry.getType())
                .content(entry.getContent())
                .title(entry.getTitle())
                .url(entry.getUrl())
                .filePath(entry.getFilePath())
                .metadata(entry.getMetadata())
                .tags(entry.getTags())
                .summary(entry.getSummary())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .enrichedAt(entry.getEnrichedAt())
                .build();
    }
}
