package com.lab.dto.response;

import com.lab.entity.ReagentInventory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReagentInventoryDTO {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String categoryHazardClass;
    private String barcode;
    private UUID locationId;
    private String locationPath;
    private String batchNo;
    private BigDecimal totalQuantity;
    private BigDecimal remainingQuantity;
    private String unit;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private LocalDate receivedDate;
    private String supplier;
    private String status;
    private String storageConditions;
    private String alertLevel;
    private LocalDateTime createdAt;

    public static ReagentInventoryDTO fromEntity(ReagentInventory inventory) {
        ReagentInventoryDTO dto = new ReagentInventoryDTO();
        dto.setId(inventory.getId());
        dto.setCategoryId(inventory.getCategoryId());
        if (inventory.getCategory() != null) {
            dto.setCategoryName(inventory.getCategory().getName());
            dto.setCategoryHazardClass(inventory.getCategory().getHazardClass());
        }
        dto.setBarcode(inventory.getBarcode());
        if (inventory.getLocation() != null) {
            dto.setLocationId(inventory.getLocation().getId());
            dto.setLocationPath(inventory.getLocation().getPath());
        }
        dto.setBatchNo(inventory.getBatchNo());
        dto.setTotalQuantity(inventory.getTotalQuantity());
        dto.setRemainingQuantity(inventory.getRemainingQuantity());
        dto.setUnit(inventory.getUnit());
        dto.setManufactureDate(inventory.getManufactureDate());
        dto.setExpiryDate(inventory.getExpiryDate());
        dto.setReceivedDate(inventory.getReceivedDate());
        dto.setSupplier(inventory.getSupplier());
        dto.setStatus(inventory.getStatus());
        dto.setStorageConditions(inventory.getStorageConditions());
        dto.setAlertLevel(inventory.getAlertLevel());
        dto.setCreatedAt(inventory.getCreatedAt());
        return dto;
    }
}
