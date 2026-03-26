package com.secondbrain.controller;

import com.secondbrain.service.WritingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assist")
@RequiredArgsConstructor
public class WritingAssistantController {

    private final WritingAssistantService writingAssistantService;

    /**
     * Get writing suggestions based on partial text
     * POST /api/assist/suggest
     */
    @PostMapping("/suggest")
    public ResponseEntity<WritingAssistantService.WritingSuggestions> getSuggestions(@RequestBody SuggestionRequest request) {
        WritingAssistantService.WritingSuggestions suggestions = writingAssistantService.getSuggestions(request.getText());
        return ResponseEntity.ok(suggestions);
    }

    @lombok.Data
    public static class SuggestionRequest {
        private String text;
    }
}
