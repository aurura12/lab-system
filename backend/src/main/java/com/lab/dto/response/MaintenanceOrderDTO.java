package com.lab.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceOrderDTO {
    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private String title;
    private String type;
    private String priority;
    private String status;
    private UUID assigneeId;
    private String assigneeName;
    private UUID creatorId;
    private String creatorName;
    private String description;
    private String resolution;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private BigDecimal cost;
    private LocalDate createdAt;
}
