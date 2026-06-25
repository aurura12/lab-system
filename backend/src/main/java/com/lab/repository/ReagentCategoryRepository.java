package com.lab.repository;

import com.lab.entity.ReagentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReagentCategoryRepository extends JpaRepository<ReagentCategory, UUID> {

    Optional<ReagentCategory> findByCasNo(String casNo);

    boolean existsByCasNo(String casNo);

    @Query("SELECT c FROM ReagentCategory c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.casNo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ReagentCategory> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<ReagentCategory> findByHazardClass(String hazardClass, Pageable pageable);
}
