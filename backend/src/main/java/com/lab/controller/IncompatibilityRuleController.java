package com.lab.controller;

import com.lab.dto.request.IncompatibilityCheckRequest;
import com.lab.dto.request.IncompatibilityRuleRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.IncompatibilityCheckResult;
import com.lab.dto.response.IncompatibilityRuleDTO;
import com.lab.dto.response.PageResponse;
import com.lab.service.IncompatibilityRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incompatibility-rules")
@RequiredArgsConstructor
public class IncompatibilityRuleController {

    private final IncompatibilityRuleService ruleService;

    @GetMapping
    public ApiResponse<PageResponse<IncompatibilityRuleDTO>> getRules(
            @RequestParam(required = false) String scenario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IncompatibilityRuleDTO> result = ruleService.getRules(scenario, page, size);
        return ApiResponse.success(new PageResponse<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<IncompatibilityRuleDTO> getById(@PathVariable UUID id) {
        return ApiResponse.success(ruleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<IncompatibilityRuleDTO> create(@Valid @RequestBody IncompatibilityRuleRequest request) {
        return ApiResponse.success("规则已创建", ruleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<IncompatibilityRuleDTO> update(@PathVariable UUID id,
                                                       @Valid @RequestBody IncompatibilityRuleRequest request) {
        return ApiResponse.success("规则已更新", ruleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        ruleService.delete(id);
        return ApiResponse.success("规则已删除", null);
    }

    @PostMapping("/check")
    public ApiResponse<IncompatibilityCheckResult> check(@RequestBody IncompatibilityCheckRequest request) {
        return ApiResponse.success(ruleService.check(request.getCategoryIds(), request.getScenario()));
    }
}
