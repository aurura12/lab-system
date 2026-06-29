package com.lab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "equipment_logs")
public class EquipmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "equipment_id", insertable = false, updatable = false)
    private UUID equipmentId;

    @Column(name = "log_type", nullable = false, length = 30)
    private String logType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "recorded_by", insertable = false, updatable = false)
    private UUID recordedById;

    @Column(name = "anomaly_flag", nullable = false)
    private Boolean anomalyFlag = false;

    @Column(name = "sensor_data", columnDefinition = "JSONB")
    private String sensorData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentLog)) return false;
        return id != null && id.equals(((EquipmentLog) o).id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
