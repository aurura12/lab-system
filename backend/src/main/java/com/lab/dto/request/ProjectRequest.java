package com.lab.dto.request;

import com.lab.entity.Project;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String code;

    private String description;

    private Project.Status status;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID ownerId;
}
