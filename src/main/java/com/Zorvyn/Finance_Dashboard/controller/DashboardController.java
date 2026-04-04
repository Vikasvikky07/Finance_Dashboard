package com.Zorvyn.Finance_Dashboard.controller;

import com.Zorvyn.Finance_Dashboard.dto.DashboardSummaryResponse;
import com.Zorvyn.Finance_Dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }
}
