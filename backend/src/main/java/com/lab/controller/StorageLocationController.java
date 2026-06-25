package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.StorageLocationDTO;
import com.lab.dto.request.StorageLocationRequest;
import com.lab.service.StorageLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage-locations")
@RequiredArgsConstructor
public class StorageLocationController {

    private final StorageLocationService locationService;

    @GetMapping("/tree")
    public ApiResponse<List<StorageLocationDTO>> getTree(@RequestParam UUID roomId) {
        return ApiResponse.success(locationService.getTreeByRoomId(roomId));
    }

    @GetMapping("/{id}")
    public ApiResponse<StorageLocationDTO> getById(@PathVariable UUID id) {
        return ApiResponse.success(locationService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<StorageLocationDTO> create(@RequestBody StorageLocationRequest request) {
        return ApiResponse.success("位置已创建", locationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<StorageLocationDTO> update(@PathVariable UUID id, @RequestBody StorageLocationRequest request) {
        return ApiResponse.success("位置已更新", locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ApiResponse.success("位置已删除", null);
    }
}
