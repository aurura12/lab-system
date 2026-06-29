package com.lab.controller;

import com.lab.dto.request.EquipmentBookingRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.EquipmentBookingDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.User;
import com.lab.service.EquipmentBookingService;
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
@RequestMapping("/api/v1/equipment-bookings")
@RequiredArgsConstructor
public class EquipmentBookingController {

    private final EquipmentBookingService bookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<PageResponse<EquipmentBookingDTO>> getBookings(
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EquipmentBookingDTO> result = bookingService.getBookings(equipmentId, status, page, size);
        return ApiResponse.success(new PageResponse<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/my")
    public ApiResponse<List<EquipmentBookingDTO>> getMyBookings(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ApiResponse.success(bookingService.getMyBookings(user.getId(), 0, 100));
    }

    @GetMapping("/{id}")
    public ApiResponse<EquipmentBookingDTO> getById(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.getById(id));
    }

    @PostMapping
    public ApiResponse<EquipmentBookingDTO> create(@Valid @RequestBody EquipmentBookingRequest request,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ApiResponse.success("预约已创建", bookingService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<EquipmentBookingDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody EquipmentBookingRequest request,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ApiResponse.success("预约已更新", bookingService.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(@PathVariable UUID id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        bookingService.cancel(id, user.getId());
        return ApiResponse.success("预约已取消", null);
    }

    @PutMapping("/{id}/checkin")
    public ApiResponse<EquipmentBookingDTO> checkin(@PathVariable UUID id) {
        return ApiResponse.success("签到成功", bookingService.checkin(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<EquipmentBookingDTO> approve(@PathVariable UUID id,
                                                     @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String note = (String) body.get("note");
        return ApiResponse.success(approved ? "预约已通过" : "预约已拒绝", bookingService.approve(id, approved, note));
    }

    @PostMapping("/release-no-shows")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<String> releaseNoShows() {
        int count = bookingService.releaseNoShows();
        return ApiResponse.success("已释放 " + count + " 个爽约时段");
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<List<EquipmentBookingDTO>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(bookingService.getPendingApprovals(page, size));
    }
}
