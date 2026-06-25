package com.lab.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReagentUseRequest {
    private BigDecimal quantity;
    private String projectId;
    private String purpose;
}
