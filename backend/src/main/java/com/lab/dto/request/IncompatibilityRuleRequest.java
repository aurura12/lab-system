package com.lab.dto.request;

import lombok.Data;

@Data
public class IncompatibilityRuleRequest {
    private String categoryAId;
    private String categoryBId;
    private String hazardType;
    private String severity;
    private String description;
    private String actionRequired;
    private String scenario;
}
