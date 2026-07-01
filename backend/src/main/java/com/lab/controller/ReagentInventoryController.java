package com.lab.controller;

import com.lab.dto.request.BatchUseRequest;
import com.lab.dto.request.ReagentInventoryRequest;
import com.lab.dto.request.ReagentUseRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.IncompatibilityCheckResult;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentInventoryDTO;
import com.lab.entity.User;
import com.lab.repository.UserRepository;
import com.lab.service.IncompatibilityRuleService;
import com.lab.service.ReagentInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reagent-inventory")
@RequiredArgsConstructor
public class ReagentInventoryController {

    private final ReagentInventoryService inventoryService;
    private final IncompatibilityRuleService incompatibilityService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<PageResponse<ReagentInventoryDTO>> getInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
                inventoryService.getInventory(keyword, status, alertLevel, categoryId, locationId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReagentInventoryDTO> getById(@PathVariable UUID id) {
        return ApiResponse.success(inventoryService.getById(id));
    }

    @GetMapping("/expiring")
    public ApiResponse<List<ReagentInventoryDTO>> getExpiring() {
        return ApiResponse.success(inventoryService.getExpiring());
    }

    @GetMapping("/out-of-stock")
    public ApiResponse<List<ReagentInventoryDTO>> getOutOfStock() {
        return ApiResponse.success(inventoryService.getOutOfStock());
    }

    @GetMapping("/fefo")
    public ApiResponse<List<ReagentInventoryDTO>> recommendFefo(@RequestParam UUID categoryId) {
        return ApiResponse.success(inventoryService.recommendFefo(categoryId));
    }

    @PostMapping("/inbound")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ReagentInventoryDTO> inbound(@Valid @RequestBody ReagentInventoryRequest request,
                                                     Authentication authentication) {
        User user = resolveUser(authentication);
        return ApiResponse.success("入库成功", inventoryService.inbound(request, user.getId()));
    }

    @PostMapping("/{id}/use")
    public ApiResponse<ReagentInventoryDTO> use(@PathVariable UUID id,
                                                  @Valid @RequestBody ReagentUseRequest request,
                                                  Authentication authentication) {
        User user = resolveUser(authentication);
        return ApiResponse.success("出库成功", inventoryService.useSingle(id, request, user.getId()));
    }

    @PostMapping("/batch-use")
    public ApiResponse<List<ReagentInventoryDTO>> batchUse(@Valid @RequestBody BatchUseRequest request,
                                                            Authentication authentication) {
        User user = resolveUser(authentication);
        return ApiResponse.success("批次出库成功", inventoryService.batchUse(request, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ReagentInventoryDTO> update(@PathVariable UUID id,
                                                    @RequestBody ReagentInventoryRequest request) {
        return ApiResponse.success("库存已更新", inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ApiResponse.success("库存已删除", null);
    }

    @PostMapping("/check-location")
    public ApiResponse<IncompatibilityCheckResult> checkLocation(
            @RequestParam UUID locationId, @RequestParam UUID categoryId) {
        return ApiResponse.success(incompatibilityService.checkLocation(locationId, categoryId, "storage"));
    }

    @PostMapping("/update-alert-levels")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> updateAlertLevels() {
        inventoryService.updateAlertLevels();
        return ApiResponse.success("预警级别已更新", null);
    }

    private User resolveUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }
}
