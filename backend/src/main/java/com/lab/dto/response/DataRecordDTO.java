package com.lab.dto.response;

import com.lab.entity.DataRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DataRecordDTO {

    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private UUID projectId;
    private String projectName;
    private UUID userId;
    private String userName;
    private UUID usageRecordId;
    private String dataType;
    private String fileName;
    private Long fileSize;
    private String description;
    private LocalDateTime createdAt;

    public static DataRecordDTO fromEntity(DataRecord record) {
        DataRecordDTO dto = new DataRecordDTO();
        dto.setId(record.getId());
        if (record.getEquipment() != null) {
            dto.setEquipmentId(record.getEquipment().getId());
            dto.setEquipmentName(record.getEquipment().getName());
        }
        if (record.getProject() != null) {
            dto.setProjectId(record.getProject().getId());
            dto.setProjectName(record.getProject().getName());
        }
        if (record.getUser() != null) {
            dto.setUserId(record.getUser().getId());
            dto.setUserName(record.getUser().getRealName());
        }
        if (record.getUsageRecord() != null) {
            dto.setUsageRecordId(record.getUsageRecord().getId());
        }
        dto.setDataType(record.getDataType());
        dto.setFileName(record.getFileName());
        dto.setFileSize(record.getFileSize());
        dto.setDescription(record.getDescription());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
