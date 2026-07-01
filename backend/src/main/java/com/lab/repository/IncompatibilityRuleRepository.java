package com.lab.repository;

import com.lab.entity.IncompatibilityRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncompatibilityRuleRepository extends JpaRepository<IncompatibilityRule, UUID> {

    Optional<IncompatibilityRule> findByCategoryAIdAndCategoryBId(UUID categoryAId, UUID categoryBId);

    @Query("SELECT r FROM IncompatibilityRule r WHERE " +
           "(r.categoryAId = :id1 AND r.categoryBId = :id2) OR " +
           "(r.categoryAId = :id2 AND r.categoryBId = :id1)")
    Optional<IncompatibilityRule> findBetween(@Param("id1") UUID id1, @Param("id2") UUID id2);

    @Query("SELECT r FROM IncompatibilityRule r WHERE " +
           "r.categoryAId = :categoryId OR r.categoryBId = :categoryId")
    List<IncompatibilityRule> findByAnyCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT r FROM IncompatibilityRule r WHERE " +
           "r.categoryAId IN :ids AND r.categoryBId IN :ids")
    List<IncompatibilityRule> findWithinSet(@Param("ids") List<UUID> ids);

    @Query("SELECT r FROM IncompatibilityRule r WHERE " +
           "(r.categoryAId = :targetId AND r.categoryBId IN :existingIds) OR " +
           "(r.categoryBId = :targetId AND r.categoryAId IN :existingIds)")
    List<IncompatibilityRule> findBetweenTargetAndSet(
            @Param("targetId") UUID targetId, @Param("existingIds") List<UUID> existingIds);

    @Query("SELECT r FROM IncompatibilityRule r WHERE " +
           "r.scenario = :scenario OR r.scenario = 'all'")
    List<IncompatibilityRule> findByScenario(@Param("scenario") String scenario);
}
