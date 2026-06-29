package com.lab.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentBookingRequest {
    private String equipmentId;
    private String projectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String experimentType;
}
