package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "floors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lab_id", "floor_number"})
})
public class Floor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(length = 100)
    private String name;
}
