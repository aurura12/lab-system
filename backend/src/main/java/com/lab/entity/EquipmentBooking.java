package com.lab.entity;

import com.lab.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "equipment_bookings")
public class EquipmentBooking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "equipment_id", insertable = false, updatable = false)
    private UUID equipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "project_id", insertable = false, updatable = false)
    private UUID projectId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "experiment_type", length = 100)
    private String experimentType;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "approval_note", columnDefinition = "TEXT")
    private String approvalNote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentBooking)) return false;
        return getId() != null && getId().equals(((EquipmentBooking) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum Status {
        pending, approved, rejected, checked_in, no_show, completed, cancelled
    }
}
