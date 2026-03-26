package com.secondbrain.controller;

import com.secondbrain.service.DebateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debate")
@RequiredArgsConstructor
public class DebateController {

    private final DebateService debateService;

    /**
     * Stress-test a claim against your knowledge base
     * POST /api/debate/stress-test
     */
    @PostMapping("/stress-test")
    public ResponseEntity<DebateService.StressTestResult> stressTestClaim(@RequestBody ClaimRequest request) {
        DebateService.StressTestResult result = debateService.stressTestClaim(request.getClaim());
        return ResponseEntity.ok(result);
    }

    @lombok.Data
    public static class ClaimRequest {
        private String claim;
    }
}
