package com.secondbrain.controller;

import com.secondbrain.dto.SearchResponse;
import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import com.secondbrain.repository.EntrySearchRepository;
import com.secondbrain.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final EntryRepository entryRepository;
    private final EntrySearchRepository entrySearchRepository;
    private final EmbeddingService embeddingService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "hybrid") String mode
    ) {
        List<Entry> results;

        switch (mode.toLowerCase()) {
            case "fts":
                results = entryRepository.fullTextSearch(query, limit);
                break;
            case "semantic":
                float[] queryEmbedding = embeddingService.generateEmbedding(query);
                results = entrySearchRepository.findSimilar(queryEmbedding, limit);
                break;
            case "hybrid":
            default:
                float[] embedding = embeddingService.generateEmbedding(query);
                results = entrySearchRepository.hybridSearch(query, embedding, limit);
                break;
        }

        SearchResponse response = SearchResponse.builder()
                .query(query)
                .totalResults(results.size())
                .results(results.stream()
                        .map(this::toSearchResult)
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/similar/{id}")
    public ResponseEntity<SearchResponse> findSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Entry entry = entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found: " + id));

        if (entry.getEmbedding() == null) {
            throw new RuntimeException("Entry has not been enriched with embeddings yet");
        }

        List<Entry> similar = entrySearchRepository.findSimilarExcluding(
                id,
                entry.getEmbedding(),
                limit
        );

        SearchResponse response = SearchResponse.builder()
                .query("Similar to entry " + id)
                .totalResults(similar.size())
                .results(similar.stream()
                        .map(this::toSearchResult)
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tags/{tag}")
    public ResponseEntity<SearchResponse> searchByTag(@PathVariable String tag) {
        List<Entry> results = entryRepository.findByTagContainingIgnoreCase(tag);

        SearchResponse response = SearchResponse.builder()
                .query("Tag: " + tag)
                .totalResults(results.size())
                .results(results.stream()
                        .map(this::toSearchResult)
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    private SearchResponse.SearchResult toSearchResult(Entry entry) {
        String snippet = entry.getContent().length() > 200
                ? entry.getContent().substring(0, 200) + "..."
                : entry.getContent();

        return SearchResponse.SearchResult.builder()
                .id(entry.getId())
                .type(entry.getType().toString())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(entry.getTags())
                .snippet(snippet)
                .build();
    }
}
