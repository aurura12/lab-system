package com.lab.controller;

import com.lab.dto.DashboardDTO;
import com.lab.dto.response.ApiResponse;
import com.lab.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ApiResponse<DashboardDTO.Overview> getOverview() {
        return ApiResponse.success(dashboardService.getOverview());
    }

    @GetMapping("/equipment-utilization")
    public ApiResponse<List<DashboardDTO.EquipmentUtilization>> getEquipmentUtilization() {
        return ApiResponse.success(dashboardService.getEquipmentUtilization());
    }

    @GetMapping("/usage-trend")
    public ApiResponse<List<DashboardDTO.UsageTrend>> getUsageTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(dashboardService.getUsageTrend(days));
    }

    @GetMapping("/project-data-distribution")
    public ApiResponse<List<DashboardDTO.ProjectDataDistribution>> getProjectDataDistribution() {
        return ApiResponse.success(dashboardService.getProjectDataDistribution());
    }

    @GetMapping("/recent-activity")
    public ApiResponse<List<DashboardDTO.RecentActivity>> getRecentActivity() {
        return ApiResponse.success(dashboardService.getRecentActivity());
    }
}
