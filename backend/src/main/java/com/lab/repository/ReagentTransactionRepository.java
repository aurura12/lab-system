package com.lab.repository;

import com.lab.entity.ReagentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReagentTransactionRepository extends JpaRepository<ReagentTransaction, UUID> {

    Page<ReagentTransaction> findByInventoryId(UUID inventoryId, Pageable pageable);

    Page<ReagentTransaction> findByType(String type, Pageable pageable);

    @Query("SELECT t FROM ReagentTransaction t WHERE " +
           "t.createdAt BETWEEN :start AND :end")
    List<ReagentTransaction> findByTimeRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM ReagentTransaction t WHERE " +
           "t.inventoryId IN (SELECT i.id FROM ReagentInventory i WHERE i.barcode = :barcode)")
    List<ReagentTransaction> findByBarcode(@Param("barcode") String barcode);

    List<ReagentTransaction> findByProjectId(UUID projectId);

    long countByTypeAndCreatedAtBetween(String type, LocalDateTime start, LocalDateTime end);
}
