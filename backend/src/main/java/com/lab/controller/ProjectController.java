package com.lab.controller;

import com.lab.dto.request.ProjectRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ProjectDTO;
import com.lab.entity.ProjectMember;
import com.lab.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<PageResponse<ProjectDTO>> getProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(projectService.getProjects(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDTO> getProjectById(@PathVariable UUID id) {
        return ApiResponse.success(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ProjectDTO> createProject(@Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success("Project created", projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<ProjectDTO> updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success("Project updated", projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ApiResponse.success("Project deleted", null);
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<ProjectMember>> getMembers(@PathVariable UUID id) {
        return ApiResponse.success(projectService.getProjectMembers(id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Void> addMember(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String role = body.getOrDefault("roleInProject", "member");
        projectService.addMember(id, userId, role);
        return ApiResponse.success("Member added", null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ApiResponse.success("Member removed", null);
    }
}
