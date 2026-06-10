package com.lab.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UsageStartRequest {

    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;

    private UUID projectId;

    private String purpose;
}
