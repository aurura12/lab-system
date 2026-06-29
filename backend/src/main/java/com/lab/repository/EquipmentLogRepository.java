package com.lab.repository;

import com.lab.entity.EquipmentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EquipmentLogRepository extends JpaRepository<EquipmentLog, UUID> {

    Page<EquipmentLog> findByEquipmentIdOrderByCreatedAtDesc(UUID equipmentId, Pageable pageable);

    @Query("SELECT l FROM EquipmentLog l WHERE " +
           "(:equipmentId IS NULL OR l.equipmentId = :equipmentId) " +
           "AND (:logType IS NULL OR l.logType = :logType) " +
           "ORDER BY l.createdAt DESC")
    Page<EquipmentLog> findByFilters(
            @Param("equipmentId") UUID equipmentId,
            @Param("logType") String logType,
            Pageable pageable);

    @Query("SELECT l FROM EquipmentLog l WHERE l.createdAt >= :since AND l.anomalyFlag = true")
    List<EquipmentLog> findRecentAnomalies(@Param("since") LocalDateTime since);
}
