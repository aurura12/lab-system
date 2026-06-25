package com.lab.dto.response;

import com.lab.entity.ReagentTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReagentTransactionDTO {
    private UUID id;
    private UUID inventoryId;
    private String barcode;
    private String categoryName;
    private String type;
    private BigDecimal quantity;
    private UUID operatorId;
    private String operatorName;
    private String purpose;
    private UUID projectId;
    private String projectName;
    private LocalDateTime createdAt;

    public static ReagentTransactionDTO fromEntity(ReagentTransaction transaction) {
        ReagentTransactionDTO dto = new ReagentTransactionDTO();
        dto.setId(transaction.getId());
        dto.setInventoryId(transaction.getInventoryId());
        if (transaction.getInventory() != null) {
            dto.setBarcode(transaction.getInventory().getBarcode());
            if (transaction.getInventory().getCategory() != null) {
                dto.setCategoryName(transaction.getInventory().getCategory().getName());
            }
        }
        dto.setType(transaction.getType());
        dto.setQuantity(transaction.getQuantity());
        dto.setOperatorId(transaction.getOperatorId());
        if (transaction.getOperator() != null) {
            dto.setOperatorName(transaction.getOperator().getRealName());
        }
        dto.setPurpose(transaction.getPurpose());
        if (transaction.getProject() != null) {
            dto.setProjectId(transaction.getProject().getId());
            dto.setProjectName(transaction.getProject().getName());
        }
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
