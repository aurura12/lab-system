package com.lab.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EquipmentBookingDTO {
    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private UUID userId;
    private String userName;
    private String userRealName;
    private UUID projectId;
    private String projectName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String purpose;
    private String experimentType;
    private LocalDateTime checkinTime;
    private String approvalNote;
    private LocalDateTime createdAt;
}
