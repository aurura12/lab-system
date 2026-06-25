package com.lab.dto.request;

import lombok.Data;

@Data
public class ReagentCategoryRequest {
    private String name;
    private String casNo;
    private String molecularFormula;
    private String specification;
    private String hazardClass;
    private String unit;
    private Integer defaultShelfLifeDays;
    private Integer minStockThreshold;
    private String storageConditions;
}
