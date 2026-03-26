package com.secondbrain.controller;

import com.secondbrain.service.WeeklyBriefService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class WeeklyBriefController {

    private final WeeklyBriefService weeklyBriefService;

    /**
     * Get weekly intelligence brief
     * GET /api/insights/weekly-brief
     */
    @GetMapping("/weekly-brief")
    public ResponseEntity<WeeklyBriefService.WeeklyBrief> getWeeklyBrief() {
        WeeklyBriefService.WeeklyBrief brief = weeklyBriefService.generateBriefForCurrentWeek();
        return ResponseEntity.ok(brief);
    }
}
