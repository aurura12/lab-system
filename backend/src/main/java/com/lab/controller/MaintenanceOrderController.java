package com.lab.controller;

import com.lab.dto.request.MaintenanceOrderRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.MaintenanceOrderDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.User;
import com.lab.service.MaintenanceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-orders")
@RequiredArgsConstructor
public class MaintenanceOrderController {

    private final MaintenanceOrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<MaintenanceOrderDTO>> getOrders(
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaintenanceOrderDTO> result = orderService.getOrders(equipmentId, status, page, size);
        return ApiResponse.success(new PageResponse<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<MaintenanceOrderDTO>> getUpcoming() {
        return ApiResponse.success(orderService.getUpcoming());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<MaintenanceOrderDTO> create(@Valid @RequestBody MaintenanceOrderRequest request,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ApiResponse.success("工单已创建", orderService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<MaintenanceOrderDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody MaintenanceOrderRequest request) {
        return ApiResponse.success("工单已更新", orderService.update(id, request));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<MaintenanceOrderDTO> complete(@PathVariable UUID id,
                                                      @RequestBody Map<String, String> body) {
        return ApiResponse.success("工单已完成", orderService.complete(id, body.get("resolution")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ApiResponse.success("工单已删除", null);
    }
}
