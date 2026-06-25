package com.lab.controller;

import com.lab.dto.request.ReagentCategoryRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentCategoryDTO;
import com.lab.service.ReagentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reagent-categories")
@RequiredArgsConstructor
public class ReagentCategoryController {

    private final ReagentCategoryService categoryService;

    @GetMapping
    public ApiResponse<PageResponse<ReagentCategoryDTO>> getCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String hazardClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(categoryService.getCategories(keyword, hazardClass, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReagentCategoryDTO> getById(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ReagentCategoryDTO> create(@Valid @RequestBody ReagentCategoryRequest request) {
        return ApiResponse.success("品类已创建", categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ReagentCategoryDTO> update(@PathVariable UUID id,
                                                   @Valid @RequestBody ReagentCategoryRequest request) {
        return ApiResponse.success("品类已更新", categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ApiResponse.success("品类已删除", null);
    }
}
