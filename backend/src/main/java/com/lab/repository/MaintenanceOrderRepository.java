package com.lab.repository;

import com.lab.entity.MaintenanceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, UUID> {

    Page<MaintenanceOrder> findByEquipmentIdOrderByCreatedAtDesc(UUID equipmentId, Pageable pageable);

    Page<MaintenanceOrder> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT m FROM MaintenanceOrder m WHERE " +
           "(:equipmentId IS NULL OR m.equipmentId = :equipmentId) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "ORDER BY m.createdAt DESC")
    Page<MaintenanceOrder> findByFilters(
            @Param("equipmentId") UUID equipmentId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT m FROM MaintenanceOrder m WHERE m.scheduledDate IS NOT NULL " +
           "AND m.scheduledDate BETWEEN :today AND :deadline " +
           "AND m.status IN ('pending', 'in_progress') " +
           "ORDER BY m.scheduledDate ASC")
    List<MaintenanceOrder> findUpcoming(
            @Param("today") LocalDate today,
            @Param("deadline") LocalDate deadline);
}
