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
public class KnowledgeGraphService {

    private final EntryRepository entryRepository;

    /**
     * Generate graph nodes (entries)
     */
    @Transactional(readOnly = true)
    public List<GraphNode> getGraphNodes() {
        log.info("Generating graph nodes");

        List<Entry> entries = entryRepository.findAll();

        return entries.stream()
                .map(e -> GraphNode.builder()
                        .id(e.getId().toString())
                        .label(e.getTitle())
                        .type(e.getType().name())
                        .tags(e.getTags() != null ? e.getTags() : List.of())
                        .size(100 + (e.getTags() != null ? e.getTags().size() * 10 : 0))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Generate graph edges (connections between entries)
     */
    @Transactional(readOnly = true)
    public List<GraphEdge> getGraphEdges() {
        log.info("Generating graph edges");

        List<Entry> entries = entryRepository.findAll();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> addedEdges = new HashSet<>();

        // Connect entries by shared tags
        for (int i = 0; i < entries.size(); i++) {
            Entry entry1 = entries.get(i);
            if (entry1.getTags() == null) continue;

            for (int j = i + 1; j < entries.size(); j++) {
                Entry entry2 = entries.get(j);
                if (entry2.getTags() == null) continue;

                // Find common tags
                Set<String> commonTags = new HashSet<>(entry1.getTags());
                commonTags.retainAll(entry2.getTags());

                if (!commonTags.isEmpty()) {
                    String edgeId = Math.min(i, j) + "-" + Math.max(i, j);
                    if (!addedEdges.contains(edgeId)) {
                        edges.add(GraphEdge.builder()
                                .source(entry1.getId().toString())
                                .target(entry2.getId().toString())
                                .label(String.join(", ", commonTags))
                                .weight(commonTags.size())
                                .type("shared_tags")
                                .build());
                        addedEdges.add(edgeId);
                    }
                }
            }
        }

        return edges;
    }

    /**
     * Get graph structure (nodes + edges)
     */
    @Transactional(readOnly = true)
    public KnowledgeGraph getFullGraph() {
        return KnowledgeGraph.builder()
                .nodes(getGraphNodes())
                .edges(getGraphEdges())
                .build();
    }

    /**
     * Get connections for a specific entry
     */
    @Transactional(readOnly = true)
    public EntryConnections getEntryConnections(Long entryId) {
        log.info("Getting connections for entry {}", entryId);

        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found: " + entryId));

        if (entry.getTags() == null || entry.getTags().isEmpty()) {
            return EntryConnections.builder()
                    .entryId(entryId)
                    .connections(List.of())
                    .build();
        }

        List<Entry> allEntries = entryRepository.findAll();
        List<EntryConnection> connections = new ArrayList<>();

        for (Entry other : allEntries) {
            if (other.getId().equals(entryId) || other.getTags() == null) continue;

            // Calculate connection strength
            Set<String> commonTags = new HashSet<>(entry.getTags());
            commonTags.retainAll(other.getTags());

            if (!commonTags.isEmpty()) {
                connections.add(EntryConnection.builder()
                        .connectedId(other.getId())
                        .connectedTitle(other.getTitle())
                        .strength(commonTags.size())
                        .commonTags(new ArrayList<>(commonTags))
                        .build());
            }
        }

        // Sort by strength (most connected first)
        connections.sort(Comparator.comparingInt(EntryConnection::getStrength).reversed());

        return EntryConnections.builder()
                .entryId(entryId)
                .connections(connections)
                .build();
    }

    // DTOs
    @lombok.Data
    @lombok.Builder
    public static class GraphNode {
        private String id;
        private String label;
        private String type;
        private List<String> tags;
        private int size;
    }

    @lombok.Data
    @lombok.Builder
    public static class GraphEdge {
        private String source;
        private String target;
        private String label;
        private int weight;
        private String type;
    }

    @lombok.Data
    @lombok.Builder
    public static class KnowledgeGraph {
        private List<GraphNode> nodes;
        private List<GraphEdge> edges;
    }

    @lombok.Data
    @lombok.Builder
    public static class EntryConnections {
        private Long entryId;
        private List<EntryConnection> connections;
    }

    @lombok.Data
    @lombok.Builder
    public static class EntryConnection {
        private Long connectedId;
        private String connectedTitle;
        private int strength;
        private List<String> commonTags;
    }
}
