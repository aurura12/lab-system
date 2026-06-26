package com.lab.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IncompatibilityRuleDTO {
    private UUID id;
    private UUID categoryAId;
    private String categoryAName;
    private UUID categoryBId;
    private String categoryBName;
    private String hazardType;
    private String severity;
    private String description;
    private String actionRequired;
    private String scenario;
    private LocalDateTime createdAt;
}
