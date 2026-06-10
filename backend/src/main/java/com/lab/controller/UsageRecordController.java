package com.lab.controller;

import com.lab.dto.request.UsageStartRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.UsageRecordDTO;
import com.lab.service.UsageRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageRecordController {

    private final UsageRecordService usageRecordService;

    @GetMapping
    public ApiResponse<PageResponse<UsageRecordDTO>> getUsageRecords(
            @RequestParam(required = false) UUID equipmentId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(usageRecordService.getUsageRecords(
                equipmentId, userId, projectId, startTime, endTime, page, size));
    }

    @GetMapping("/active")
    public ApiResponse<List<UsageRecordDTO>> getActiveUsages() {
        return ApiResponse.success(usageRecordService.getActiveUsages());
    }

    @PostMapping("/start")
    public ApiResponse<UsageRecordDTO> startUsage(@Valid @RequestBody UsageStartRequest request,
                                                    Authentication authentication) {
        // In a real app, get user ID from authentication principal
        // For now, we'll need the user to pass their ID or we extract from JWT
        return ApiResponse.success("Usage started", usageRecordService.startUsage(request, null));
    }

    @PutMapping("/{id}/end")
    public ApiResponse<UsageRecordDTO> endUsage(@PathVariable UUID id) {
        return ApiResponse.success("Usage ended", usageRecordService.endUsage(id));
    }
}
