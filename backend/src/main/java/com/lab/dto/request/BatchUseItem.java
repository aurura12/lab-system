package com.lab.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BatchUseItem {
    private String inventoryId;
    private BigDecimal quantity;
}
