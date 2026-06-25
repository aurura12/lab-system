package com.lab.dto.response;

import com.lab.entity.ReagentCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReagentCategoryDTO {
    private UUID id;
    private String name;
    private String casNo;
    private String molecularFormula;
    private String specification;
    private String hazardClass;
    private String unit;
    private Integer defaultShelfLifeDays;
    private Integer minStockThreshold;
    private String storageConditions;
    private LocalDateTime createdAt;

    public static ReagentCategoryDTO fromEntity(ReagentCategory category) {
        ReagentCategoryDTO dto = new ReagentCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCasNo(category.getCasNo());
        dto.setMolecularFormula(category.getMolecularFormula());
        dto.setSpecification(category.getSpecification());
        dto.setHazardClass(category.getHazardClass());
        dto.setUnit(category.getUnit());
        dto.setDefaultShelfLifeDays(category.getDefaultShelfLifeDays());
        dto.setMinStockThreshold(category.getMinStockThreshold());
        dto.setStorageConditions(category.getStorageConditions());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
}
