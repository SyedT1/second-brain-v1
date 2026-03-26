package com.secondbrain.controller;

import com.secondbrain.service.ContradictionDetectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class ContradictionController {

    private final ContradictionDetectorService contradictionDetectorService;

    /**
     * Detect contradictions in an entry
     * GET /api/insights/contradictions/{id}
     */
    @GetMapping("/contradictions/{id}")
    public ResponseEntity<ContradictionDetectorService.ContradictionAnalysis> getContradictions(@PathVariable Long id) {
        ContradictionDetectorService.ContradictionAnalysis analysis = contradictionDetectorService.detectContradictions(id);
        return ResponseEntity.ok(analysis);
    }
}
