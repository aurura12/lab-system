package com.lab.repository;

import com.lab.entity.DataRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DataRecordRepository extends JpaRepository<DataRecord, UUID> {

    Page<DataRecord> findByEquipmentId(UUID equipmentId, Pageable pageable);

    Page<DataRecord> findByUserId(UUID userId, Pageable pageable);

    Page<DataRecord> findByProjectId(UUID projectId, Pageable pageable);

    Page<DataRecord> findByEquipmentIdAndUserId(UUID equipmentId, UUID userId, Pageable pageable);

    Page<DataRecord> findByEquipmentIdAndProjectId(UUID equipmentId, UUID projectId, Pageable pageable);

    Page<DataRecord> findByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable);

    Page<DataRecord> findByEquipmentIdAndUserIdAndProjectId(UUID equipmentId, UUID userId, UUID projectId, Pageable pageable);

    @Query("SELECT COUNT(d) FROM DataRecord d WHERE d.project.id = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT d.project.id, COUNT(d) FROM DataRecord d WHERE d.project IS NOT NULL GROUP BY d.project.id")
    java.util.List<Object[]> countByProject();
}
