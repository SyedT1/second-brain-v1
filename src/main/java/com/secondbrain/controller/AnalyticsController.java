package com.secondbrain.controller;

import com.secondbrain.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get expertise report - shows your expertise level in each topic
     * GET /api/analytics/expertise
     */
    @GetMapping("/expertise")
    public ResponseEntity<AnalyticsService.ExpertiseReport> getExpertiseReport() {
        AnalyticsService.ExpertiseReport report = analyticsService.getExpertiseReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Detect knowledge gaps - AI analyzes your knowledge base and suggests areas to learn
     * GET /api/analytics/knowledge-gaps
     */
    @GetMapping("/knowledge-gaps")
    public ResponseEntity<AnalyticsService.KnowledgeGapsReport> getKnowledgeGaps() {
        AnalyticsService.KnowledgeGapsReport report = analyticsService.detectKnowledgeGaps();
        return ResponseEntity.ok(report);
    }
}
