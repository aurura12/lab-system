package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "storage_locations")
public class StorageLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private StorageLocation parent;

    @Column(name = "parent_id", insertable = false, updatable = false)
    private UUID parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 20)
    private String level;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(length = 200)
    private String path;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageLocation)) return false;
        return getId() != null && getId().equals(((StorageLocation) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum Level {
        cabinet, shelf, grid
    }
}
