package com.lab.controller;

import com.lab.dto.request.EquipmentLogRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.EquipmentLogDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.User;
import com.lab.service.EquipmentLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment-logs")
@RequiredArgsConstructor
public class EquipmentLogController {

    private final EquipmentLogService logService;

    @GetMapping
    public ApiResponse<PageResponse<EquipmentLogDTO>> getLogs(
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) String logType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EquipmentLogDTO> result = logService.getLogs(equipmentId, logType, page, size);
        return ApiResponse.success(new PageResponse<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/anomalies")
    public ApiResponse<List<EquipmentLogDTO>> getRecentAnomalies(
            @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(logService.getRecentAnomalies(days));
    }

    @PostMapping
    public ApiResponse<EquipmentLogDTO> create(@Valid @RequestBody EquipmentLogRequest request,
                                                Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ApiResponse.success("日志已记录", logService.create(request, user.getId()));
    }
}
