package com.lab.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceOrderRequest {
    private String equipmentId;
    private String title;
    private String type;
    private String priority;
    private String assigneeId;
    private String description;
    private String resolution;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private BigDecimal cost;
    private String status;
}
