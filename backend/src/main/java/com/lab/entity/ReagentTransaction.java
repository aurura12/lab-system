package com.lab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reagent_transactions")
public class ReagentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private ReagentInventory inventory;

    @Column(name = "inventory_id", insertable = false, updatable = false)
    private UUID inventoryId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @Column(name = "operator_id", insertable = false, updatable = false)
    private UUID operatorId;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "project_id", insertable = false, updatable = false)
    private UUID projectId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReagentTransaction)) return false;
        return id != null && id.equals(((ReagentTransaction) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum Type {
        inbound, outbound
    }
}
