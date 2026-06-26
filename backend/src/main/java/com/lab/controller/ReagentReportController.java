package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.ReagentReportDTO;
import com.lab.service.ReagentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reagent-reports")
@RequiredArgsConstructor
public class ReagentReportController {

    private final ReagentReportService reportService;

    @GetMapping("/usage")
    public ApiResponse<ReagentReportDTO> getUsageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(reportService.getReport(startDate, endDate));
    }
}
