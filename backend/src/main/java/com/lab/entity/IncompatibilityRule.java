package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "incompatibility_rules")
public class IncompatibilityRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_a_id", nullable = false)
    private ReagentCategory categoryA;

    @Column(name = "category_a_id", insertable = false, updatable = false)
    private UUID categoryAId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_b_id", nullable = false)
    private ReagentCategory categoryB;

    @Column(name = "category_b_id", insertable = false, updatable = false)
    private UUID categoryBId;

    @Column(name = "hazard_type", nullable = false, length = 50)
    private String hazardType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "action_required", columnDefinition = "TEXT")
    private String actionRequired;

    @Column(nullable = false, length = 50)
    private String scenario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncompatibilityRule)) return false;
        return getId() != null && getId().equals(((IncompatibilityRule) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
