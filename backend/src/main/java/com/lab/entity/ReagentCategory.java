package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reagent_categories")
public class ReagentCategory extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "cas_no", length = 50, unique = true)
    private String casNo;

    @Column(name = "molecular_formula", length = 100)
    private String molecularFormula;

    @Column(length = 100)
    private String specification;

    @Column(name = "hazard_class", nullable = false, length = 50)
    private String hazardClass = "none";

    @Column(nullable = false, length = 20)
    private String unit = "瓶";

    @Column(name = "default_shelf_life_days")
    private Integer defaultShelfLifeDays;

    @Column(name = "min_stock_threshold")
    private Integer minStockThreshold;

    @Column(columnDefinition = "TEXT")
    private String storageConditions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReagentCategory)) return false;
        return getId() != null && getId().equals(((ReagentCategory) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum HazardClass {
        flammable, corrosive, oxidizing, toxic, none
    }
}
