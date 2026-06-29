package com.lab.dto.request;

import lombok.Data;

@Data
public class EquipmentLogRequest {
    private String equipmentId;
    private String logType;
    private String description;
    private Boolean anomalyFlag;
    private String sensorData;
}
