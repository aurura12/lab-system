package com.lab.repository;

import com.lab.entity.ReagentInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReagentInventoryRepository extends JpaRepository<ReagentInventory, UUID> {

    Optional<ReagentInventory> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    @Query("SELECT i FROM ReagentInventory i WHERE " +
           "i.category.id IN (SELECT c.id FROM ReagentCategory c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "OR LOWER(i.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.batchNo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ReagentInventory> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<ReagentInventory> findByStatus(String status, Pageable pageable);

    Page<ReagentInventory> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<ReagentInventory> findByLocationId(UUID locationId, Pageable pageable);

    List<ReagentInventory> findByCategoryIdAndStatusNotInOrderByExpiryDateAsc(
            UUID categoryId, List<String> excludedStatuses);

    @Query("SELECT i FROM ReagentInventory i WHERE " +
           "i.alertLevel IN :levels ORDER BY i.expiryDate ASC")
    List<ReagentInventory> findByAlertLevelIn(@Param("levels") List<String> levels);

    long countByAlertLevel(String alertLevel);
}
