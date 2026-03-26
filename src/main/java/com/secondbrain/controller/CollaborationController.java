package com.secondbrain.controller;

import com.secondbrain.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collaborate")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    /**
     * Generate collaboration report for two topics
     * POST /api/collaborate/compare
     */
    @PostMapping("/compare")
    public ResponseEntity<CollaborationService.CollaborationReport> compareTopics(@RequestBody CompareRequest request) {
        CollaborationService.CollaborationReport report = collaborationService.generateCollaborationReport(
                request.getTopic1(),
                request.getTopic2()
        );
        return ResponseEntity.ok(report);
    }

    @lombok.Data
    public static class CompareRequest {
        private String topic1;
        private String topic2;
    }
}
