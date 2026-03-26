package com.secondbrain.controller;

import com.secondbrain.service.TransformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transform")
@RequiredArgsConstructor
public class TransformationController {

    private final TransformationService transformationService;

    /**
     * Transform an entry into different formats
     * POST /api/transform/{id}?format=TWEET_THREAD
     */
    @PostMapping("/{id}")
    public ResponseEntity<TransformationResponse> transformEntry(
            @PathVariable Long id,
            @RequestParam TransformationService.TransformFormat format) {

        String transformed = transformationService.transformEntry(id, format);

        return ResponseEntity.ok(new TransformationResponse(
                id,
                format.name(),
                transformed
        ));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TransformationResponse {
        private Long entryId;
        private String format;
        private String transformed;
    }
}
