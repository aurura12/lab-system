package com.lab.repository;

import com.lab.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    List<Equipment> findByRoomId(UUID roomId);

    Page<Equipment> findByRoomId(UUID roomId, Pageable pageable);

    Page<Equipment> findByStatus(Equipment.Status status, Pageable pageable);

    Page<Equipment> findByCategory(Equipment.Category category, Pageable pageable);

    @Query("SELECT e FROM Equipment e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Equipment> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(Equipment.Status status);

    @Query("SELECT e FROM Equipment e WHERE e.nextMaintenance IS NOT NULL " +
           "AND e.nextMaintenance <= :deadline " +
           "AND e.status <> 'retired'")
    List<Equipment> findByNextMaintenanceBefore(@Param("deadline") LocalDate deadline);
}
