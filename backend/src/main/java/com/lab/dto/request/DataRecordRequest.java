package com.lab.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class DataRecordRequest {

    private UUID equipmentId;

    private UUID usageRecordId;

    private UUID projectId;

    private UUID userId;

    private String dataType;

    private String description;

    private String metadataJson;
}
