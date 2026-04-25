package com.contactpro.contactpro.controller;

import org.springframework.web.bind.annotation.*;
import com.contactpro.contactpro.dto.AnalyticsResponse;
import com.contactpro.contactpro.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/user/{userId}")
    public AnalyticsResponse getAnalytics(@PathVariable Long userId) {
        return analyticsService.getUserAnalytics(userId);
    }
}