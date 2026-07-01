package com.lab.repository;

import com.lab.entity.EquipmentBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentBookingRepository extends JpaRepository<EquipmentBooking, UUID> {

    Optional<EquipmentBooking> findByEquipmentIdAndUserIdAndStartTime(
            UUID equipmentId, UUID userId, LocalDateTime startTime);

    boolean existsByEquipmentIdAndStartTime(UUID equipmentId, LocalDateTime startTime);

    @Query("SELECT b FROM EquipmentBooking b WHERE b.equipmentId = :equipmentId " +
           "AND b.startTime < :end AND b.endTime > :start " +
           "AND b.status IN ('pending','approved','checked_in')")
    List<EquipmentBooking> findConflicting(
            @Param("equipmentId") UUID equipmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<EquipmentBooking> findByUserIdOrderByStartTimeDesc(UUID userId, Pageable pageable);

    List<EquipmentBooking> findByStatusAndEndTimeBefore(String status, LocalDateTime time);

    @Query("SELECT b FROM EquipmentBooking b WHERE b.status = 'approved' " +
           "AND b.startTime < :threshold AND b.checkinTime IS NULL")
    List<EquipmentBooking> findNoShows(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT b FROM EquipmentBooking b WHERE " +
           "(:equipmentId IS NULL OR b.equipmentId = :equipmentId) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "ORDER BY b.startTime DESC")
    Page<EquipmentBooking> findByFilters(
            @Param("equipmentId") UUID equipmentId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT b FROM EquipmentBooking b WHERE b.status = 'pending' " +
           "ORDER BY b.startTime ASC")
    List<EquipmentBooking> findPendingApprovals(Pageable pageable);

    @Query("SELECT COUNT(b) FROM EquipmentBooking b WHERE b.userId = :userId " +
           "AND b.status = 'no_show' AND b.startTime > :since")
    long countNoShowsSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
