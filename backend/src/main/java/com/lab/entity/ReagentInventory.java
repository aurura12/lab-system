package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reagent_inventory")
public class ReagentInventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ReagentCategory category;

    @Column(name = "category_id", insertable = false, updatable = false)
    private UUID categoryId;

    @Column(length = 100, unique = true)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StorageLocation location;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "total_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal remainingQuantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(length = 150)
    private String supplier;

    @Column(nullable = false, length = 20)
    private String status = "unopened";

    @Column(columnDefinition = "TEXT")
    private String storageConditions;

    @Column(name = "alert_level", nullable = false, length = 20)
    private String alertLevel = "normal";

    public enum Status {
        unopened, opened, expired, disposed
    }

    public enum AlertLevel {
        normal, warning_days, warning_urgent, expired
    }
}
