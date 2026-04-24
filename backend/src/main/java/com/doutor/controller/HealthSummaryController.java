package com.doutor.controller;

import com.doutor.dto.response.HealthSummaryResponse;
import com.doutor.service.HealthSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-summary")
@RequiredArgsConstructor
public class HealthSummaryController {

    private final HealthSummaryService healthSummaryService;

    @GetMapping
    public ResponseEntity<HealthSummaryResponse> get() {
        return ResponseEntity.ok(healthSummaryService.getSummary(false));
    }

    @PostMapping("/refresh")
    public ResponseEntity<HealthSummaryResponse> refresh() {
        return ResponseEntity.ok(healthSummaryService.getSummary(true));
    }
}
