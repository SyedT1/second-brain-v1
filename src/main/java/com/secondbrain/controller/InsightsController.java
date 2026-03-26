package com.secondbrain.controller;

import com.secondbrain.dto.EntryResponse;
import com.secondbrain.service.InsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    /**
     * Get entries you haven't revisited in X days
     * Example: GET /api/insights/resurface?days=30
     */
    @GetMapping("/resurface")
    public ResponseEntity<List<EntryResponse>> getResurfacedEntries(
            @RequestParam(defaultValue = "30") int days) {
        List<EntryResponse> entries = insightsService.getResurfacedEntries(days);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get forgotten knowledge categorized by time periods
     * GET /api/insights/forgotten
     */
    @GetMapping("/forgotten")
    public ResponseEntity<InsightsService.ForgottenKnowledge> getForgottenKnowledge() {
        InsightsService.ForgottenKnowledge forgotten = insightsService.getForgottenKnowledge();
        return ResponseEntity.ok(forgotten);
    }
}
