package com.lab.repository;

import com.lab.entity.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    Optional<UsageRecord> findByEquipmentIdAndStatus(UUID equipmentId, UsageRecord.Status status);

    List<UsageRecord> findByStatus(UsageRecord.Status status);

    Page<UsageRecord> findByEquipmentId(UUID equipmentId, Pageable pageable);

    Page<UsageRecord> findByUserId(UUID userId, Pageable pageable);

    Page<UsageRecord> findByProjectId(UUID projectId, Pageable pageable);

    Page<UsageRecord> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<UsageRecord> findByEquipmentIdAndLoginTimeBetween(UUID equipmentId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<UsageRecord> findByUserIdAndLoginTimeBetween(UUID userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<UsageRecord> findByProjectIdAndLoginTimeBetween(UUID projectId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<UsageRecord> findByEquipmentIdAndUserId(UUID equipmentId, UUID userId, Pageable pageable);

    Page<UsageRecord> findByEquipmentIdAndProjectId(UUID equipmentId, UUID projectId, Pageable pageable);

    Page<UsageRecord> findByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable);

    Page<UsageRecord> findByEquipmentIdAndUserIdAndProjectId(UUID equipmentId, UUID userId, UUID projectId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, u.loginTime, COALESCE(u.logoutTime, CURRENT_TIMESTAMP))), 0) " +
           "FROM UsageRecord u WHERE u.equipment.id = :equipmentId AND u.loginTime >= :startTime")
    long sumUsageSecondsByEquipmentAndTimeRange(@Param("equipmentId") UUID equipmentId,
                                                 @Param("startTime") LocalDateTime startTime);

    @Query(value = "SELECT DATE(u.login_time) as usage_date, " +
                   "COALESCE(SUM(EXTRACT(EPOCH FROM (COALESCE(u.logout_time, NOW()) - u.login_time))), 0) as total_seconds " +
                   "FROM usage_records u WHERE u.login_time >= :startTime " +
                   "GROUP BY DATE(u.login_time) ORDER BY usage_date",
           nativeQuery = true)
    List<Object[]> findDailyUsageTrend(@Param("startTime") LocalDateTime startTime);
}
