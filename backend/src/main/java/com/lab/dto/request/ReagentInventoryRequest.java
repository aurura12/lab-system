package com.lab.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReagentInventoryRequest {
    private String categoryId;
    private String barcode;
    private String locationId;
    private String batchNo;
    private BigDecimal totalQuantity;
    private String unit;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private String supplier;
    private String storageConditions;
}
