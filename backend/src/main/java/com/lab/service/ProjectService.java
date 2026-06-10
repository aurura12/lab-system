package com.lab.service;

import com.lab.dto.request.ProjectRequest;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ProjectDTO;
import com.lab.entity.Project;
import com.lab.entity.ProjectMember;
import com.lab.entity.User;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.ProjectMemberRepository;
import com.lab.repository.ProjectRepository;
import com.lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getProjects(String keyword, int page, int size) {
        Page<Project> projectPage = projectRepository.findByKeyword(keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<ProjectDTO> dtos = projectPage.getContent().stream().map(p -> {
            ProjectDTO dto = ProjectDTO.fromEntity(p);
            dto.setMemberCount(projectMemberRepository.findByProjectId(p.getId()).size());
            return dto;
        }).toList();
        return new PageResponse<>(dtos, projectPage.getTotalElements(), projectPage.getTotalPages(), page, size);
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        ProjectDTO dto = ProjectDTO.fromEntity(project);
        dto.setMemberCount(projectMemberRepository.findByProjectId(id).size());
        return dto;
    }

    @Transactional
    public ProjectDTO createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : Project.Status.active);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getOwnerId()));
            project.setOwner(owner);
        }
        return ProjectDTO.fromEntity(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO updateProject(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setName(request.getName());
        if (request.getCode() != null) project.setCode(request.getCode());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getOwnerId()));
            project.setOwner(owner);
        }
        return ProjectDTO.fromEntity(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProjectMember> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findByProjectIdWithUser(projectId);
    }

    @Transactional
    public ProjectMember addMember(UUID projectId, UUID userId, String role) {
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BadRequestException("该用户已是项目成员");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        if (role != null) {
            member.setRoleInProject(ProjectMember.RoleInProject.valueOf(role));
            // 如果角色是负责人，同时更新项目的 owner
            if ("lead".equalsIgnoreCase(role)) {
                projectRepository.setOwnerById(projectId, userId);
            }
        }
        return projectMemberRepository.save(member);
    }

    @Transactional
    public ProjectMember addMember(UUID projectId, String userIdInput, String role) {
        UUID userId;
        try {
            userId = UUID.fromString(userIdInput);
        } catch (IllegalArgumentException e) {
            // Not a UUID — try username
            Optional<User> byUsername = userRepository.findByUsername(userIdInput);
            if (byUsername.isPresent()) {
                userId = byUsername.get().getId();
            } else {
                // Try real name
                List<User> byRealName = userRepository.findByRealName(userIdInput);
                if (byRealName.isEmpty()) {
                    throw new ResourceNotFoundException("User", "username/realName", userIdInput);
                }
                if (byRealName.size() > 1) {
                    throw new BadRequestException("找到多个同名的用户，请使用用户名或用户ID");
                }
                userId = byRealName.get(0).getId();
            }
        }
        return addMember(projectId, userId, role);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }
}
