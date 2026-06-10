package com.lab.controller;

import com.lab.dto.request.EquipmentRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.EquipmentDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.Equipment;
import com.lab.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ApiResponse<PageResponse<EquipmentDTO>> getEquipment(
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) Equipment.Status status,
            @RequestParam(required = false) Equipment.Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(equipmentService.getEquipment(roomId, status, category, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<EquipmentDTO> getEquipmentById(@PathVariable UUID id) {
        return ApiResponse.success(equipmentService.getEquipmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success("设备已创建", equipmentService.createEquipment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<EquipmentDTO> updateEquipment(@PathVariable UUID id, @Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success("设备已更新", equipmentService.updateEquipment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> deleteEquipment(@PathVariable UUID id) {
        equipmentService.deleteEquipment(id);
        return ApiResponse.success("设备已删除", null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<EquipmentDTO> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Equipment.Status status = Equipment.Status.valueOf(body.get("status"));
        return ApiResponse.success("状态已更新", equipmentService.updateStatus(id, status));
    }
}
