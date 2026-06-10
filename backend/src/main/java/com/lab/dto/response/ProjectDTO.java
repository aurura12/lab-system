package com.lab.dto.response;

import com.lab.entity.Project;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProjectDTO {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private Project.Status status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID ownerId;
    private String ownerName;
    private int memberCount;
    private LocalDateTime createdAt;

    public static ProjectDTO fromEntity(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setCode(project.getCode());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        if (project.getOwner() != null) {
            dto.setOwnerId(project.getOwner().getId());
            dto.setOwnerName(project.getOwner().getRealName());
        }
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }
}
