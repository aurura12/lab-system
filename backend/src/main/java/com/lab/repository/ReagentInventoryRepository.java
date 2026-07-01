package com.lab.repository;

import com.lab.entity.ReagentInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReagentInventoryRepository extends JpaRepository<ReagentInventory, UUID> {

    @EntityGraph(attributePaths = {"category", "location"})
    @Override
    Page<ReagentInventory> findAll(Pageable pageable);

    Optional<ReagentInventory> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    @EntityGraph(attributePaths = {"category", "location"})
    @Query("SELECT i FROM ReagentInventory i WHERE " +
           "i.category.id IN (SELECT c.id FROM ReagentCategory c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "OR LOWER(i.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.batchNo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ReagentInventory> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "location"})
    Page<ReagentInventory> findByStatus(String status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "location"})
    Page<ReagentInventory> findByAlertLevel(String alertLevel, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "location"})
    Page<ReagentInventory> findByCategoryId(UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "location"})
    Page<ReagentInventory> findByLocationId(UUID locationId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "location"})
    List<ReagentInventory> findByCategoryIdAndStatusNotInOrderByExpiryDateAsc(
            UUID categoryId, List<String> excludedStatuses);

    @EntityGraph(attributePaths = {"category", "location"})
    @Query("SELECT i FROM ReagentInventory i WHERE " +
           "i.alertLevel IN :levels ORDER BY i.expiryDate ASC")
    List<ReagentInventory> findByAlertLevelIn(@Param("levels") List<String> levels);

    long countByAlertLevel(String alertLevel);

    @EntityGraph(attributePaths = {"category", "location"})
    @Query("SELECT i FROM ReagentInventory i WHERE i.remainingQuantity <= " +
           "i.category.minStockThreshold AND i.category.minStockThreshold IS NOT NULL " +
           "AND i.status NOT IN ('disposed', 'expired')")
    List<ReagentInventory> findLowStock();

    @Query("SELECT DISTINCT i.categoryId FROM ReagentInventory i WHERE " +
           "i.locationId = :locationId AND i.status NOT IN ('disposed', 'expired')")
    List<UUID> findActiveCategoryIdsByLocationId(@Param("locationId") UUID locationId);
}
