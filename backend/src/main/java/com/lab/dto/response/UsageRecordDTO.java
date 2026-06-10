package com.lab.dto.response;

import com.lab.entity.UsageRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UsageRecordDTO {

    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private UUID userId;
    private String userName;
    private UUID projectId;
    private String projectName;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private Long durationMinutes;
    private String purpose;
    private UsageRecord.Status status;
    private String notes;

    public static UsageRecordDTO fromEntity(UsageRecord record) {
        UsageRecordDTO dto = new UsageRecordDTO();
        dto.setId(record.getId());
        if (record.getEquipment() != null) {
            dto.setEquipmentId(record.getEquipment().getId());
            dto.setEquipmentName(record.getEquipment().getName());
        }
        if (record.getUser() != null) {
            dto.setUserId(record.getUser().getId());
            dto.setUserName(record.getUser().getRealName());
        }
        if (record.getProject() != null) {
            dto.setProjectId(record.getProject().getId());
            dto.setProjectName(record.getProject().getName());
        }
        dto.setLoginTime(record.getLoginTime());
        dto.setLogoutTime(record.getLogoutTime());
        if (record.getLoginTime() != null && record.getLogoutTime() != null) {
            dto.setDurationMinutes(java.time.Duration.between(record.getLoginTime(), record.getLogoutTime()).toMinutes());
        }
        dto.setPurpose(record.getPurpose());
        dto.setStatus(record.getStatus());
        dto.setNotes(record.getNotes());
        return dto;
    }
}
