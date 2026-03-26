package com.secondbrain.controller;

import com.secondbrain.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    /**
     * Get full knowledge graph (nodes and edges)
     * GET /api/graph/full
     */
    @GetMapping("/full")
    public ResponseEntity<KnowledgeGraphService.KnowledgeGraph> getFullGraph() {
        KnowledgeGraphService.KnowledgeGraph graph = knowledgeGraphService.getFullGraph();
        return ResponseEntity.ok(graph);
    }

    /**
     * Get nodes for graph
     * GET /api/graph/nodes
     */
    @GetMapping("/nodes")
    public ResponseEntity<?> getNodes() {
        return ResponseEntity.ok(knowledgeGraphService.getGraphNodes());
    }

    /**
     * Get edges for graph
     * GET /api/graph/edges
     */
    @GetMapping("/edges")
    public ResponseEntity<?> getEdges() {
        return ResponseEntity.ok(knowledgeGraphService.getGraphEdges());
    }

    /**
     * Get connections for specific entry
     * GET /api/graph/connections/{id}
     */
    @GetMapping("/connections/{id}")
    public ResponseEntity<KnowledgeGraphService.EntryConnections> getConnections(@PathVariable Long id) {
        KnowledgeGraphService.EntryConnections connections = knowledgeGraphService.getEntryConnections(id);
        return ResponseEntity.ok(connections);
    }
}
