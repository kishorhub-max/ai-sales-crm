package com.aisalescrm.controller;

import com.aisalescrm.dto.response.DashboardStatsResponse;
import com.aisalescrm.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated KPIs and chart data")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get full dashboard stats — KPIs, charts, recent activity")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}