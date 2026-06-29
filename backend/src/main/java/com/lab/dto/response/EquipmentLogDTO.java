package com.lab.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EquipmentLogDTO {
    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private String logType;
    private String description;
    private UUID recordedById;
    private String recordedByName;
    private Boolean anomalyFlag;
    private LocalDateTime createdAt;
}
