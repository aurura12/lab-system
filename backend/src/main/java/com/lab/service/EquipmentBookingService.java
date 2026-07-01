package com.lab.service;

import com.lab.dto.request.EquipmentBookingRequest;
import com.lab.dto.response.EquipmentBookingDTO;
import com.lab.entity.EquipmentBooking;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.EquipmentBookingRepository;
import com.lab.repository.EquipmentRepository;
import com.lab.repository.ProjectRepository;
import com.lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentBookingService {

    private final EquipmentBookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private static final int NO_SHOW_MINUTES = 30;

    @Transactional(readOnly = true)
    public Page<EquipmentBookingDTO> getBookings(String equipmentId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        UUID eqId = equipmentId != null ? UUID.fromString(equipmentId) : null;
        return bookingRepository.findByFilters(eqId, status, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<EquipmentBookingDTO> getMyBookings(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return bookingRepository.findByUserIdOrderByStartTimeDesc(userId, pageable).stream()
                .map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public EquipmentBookingDTO getById(UUID id) {
        return toDTO(findBooking(id));
    }

    @Transactional
    public EquipmentBookingDTO create(EquipmentBookingRequest request, UUID userId) {
        UUID equipmentId = UUID.fromString(request.getEquipmentId());
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        // 校验
        if (!start.isBefore(end)) {
            throw new BadRequestException("开始时间必须早于结束时间");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("预约时间不能在过去");
        }

        // 爽约累计惩罚：近 30 天内爽约 3 次及以上，禁止预约
        long noShowCount = bookingRepository.countNoShowsSince(userId, LocalDateTime.now().minusDays(30));
        if (noShowCount >= 3) {
            throw new BadRequestException("您近 30 天内爽约 " + noShowCount + " 次，暂时无法预约，请联系管理员");
        }

        // 检查冲突
        List<EquipmentBooking> conflicts = bookingRepository.findConflicting(equipmentId, start, end);
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("该时段已被预约，请选择其他时间");
        }

        EquipmentBooking booking = new EquipmentBooking();
        booking.setEquipment(equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", request.getEquipmentId())));
        booking.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setPurpose(request.getPurpose());
        booking.setExperimentType(request.getExperimentType());
        booking.setStatus("pending");

        if (request.getProjectId() != null && !request.getProjectId().isEmpty()) {
            booking.setProject(projectRepository.findById(UUID.fromString(request.getProjectId())).orElse(null));
        }

        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public EquipmentBookingDTO update(UUID id, EquipmentBookingRequest request, UUID userId) {
        EquipmentBooking booking = findBooking(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("只能修改自己的预约");
        }
        if (!"pending".equals(booking.getStatus())) {
            throw new BadRequestException("只能修改待审批的预约");
        }

        LocalDateTime newStart = request.getStartTime() != null ? request.getStartTime() : booking.getStartTime();
        LocalDateTime newEnd = request.getEndTime() != null ? request.getEndTime() : booking.getEndTime();

        if (!newStart.isBefore(newEnd)) {
            throw new BadRequestException("开始时间必须早于结束时间");
        }
        if (newStart.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("预约时间不能在过去");
        }

        // 检查冲突（排除自身）
        List<EquipmentBooking> conflicts = bookingRepository.findConflicting(booking.getEquipmentId(), newStart, newEnd);
        conflicts.removeIf(c -> c.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("该时段已被预约");
        }

        if (request.getStartTime() != null) booking.setStartTime(newStart);
        if (request.getEndTime() != null) booking.setEndTime(newEnd);
        if (request.getPurpose() != null) booking.setPurpose(request.getPurpose());
        if (request.getExperimentType() != null) booking.setExperimentType(request.getExperimentType());
        if (request.getProjectId() != null) {
            booking.setProject(projectRepository.findById(UUID.fromString(request.getProjectId())).orElse(null));
        }

        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public void cancel(UUID id, UUID userId) {
        EquipmentBooking booking = findBooking(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("只能取消自己的预约");
        }
        if ("completed".equals(booking.getStatus()) || "cancelled".equals(booking.getStatus())) {
            throw new BadRequestException("该预约无法取消");
        }
        booking.setStatus("cancelled");
        bookingRepository.save(booking);
    }

    @Transactional
    public EquipmentBookingDTO checkin(UUID id) {
        EquipmentBooking booking = findBooking(id);
        if (!"approved".equals(booking.getStatus())) {
            throw new BadRequestException("当前状态无法签到");
        }
        booking.setStatus("checked_in");
        booking.setCheckinTime(LocalDateTime.now());
        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public EquipmentBookingDTO approve(UUID id, boolean approved, String note) {
        EquipmentBooking booking = findBooking(id);
        if (!"pending".equals(booking.getStatus())) {
            throw new BadRequestException("该预约已处理");
        }
        booking.setStatus(approved ? "approved" : "rejected");
        booking.setApprovalNote(note);
        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public int releaseNoShows() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(NO_SHOW_MINUTES);
        List<EquipmentBooking> noShows = bookingRepository.findNoShows(threshold);
        for (EquipmentBooking b : noShows) {
            b.setStatus("no_show");
            bookingRepository.save(b);
        }
        return noShows.size();
    }

    @Transactional(readOnly = true)
    public List<EquipmentBookingDTO> getPendingApprovals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findPendingApprovals(pageable).stream()
                .map(this::toDTO).toList();
    }

    private EquipmentBooking findBooking(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentBooking", "id", id));
    }

    private EquipmentBookingDTO toDTO(EquipmentBooking b) {
        EquipmentBookingDTO dto = new EquipmentBookingDTO();
        dto.setId(b.getId());
        dto.setEquipmentId(b.getEquipmentId());
        if (b.getEquipment() != null) dto.setEquipmentName(b.getEquipment().getName());
        dto.setUserId(b.getUserId());
        if (b.getUser() != null) {
            dto.setUserName(b.getUser().getUsername());
            dto.setUserRealName(b.getUser().getRealName());
        }
        dto.setProjectId(b.getProjectId());
        if (b.getProject() != null) dto.setProjectName(b.getProject().getName());
        dto.setStartTime(b.getStartTime());
        dto.setEndTime(b.getEndTime());
        dto.setStatus(b.getStatus());
        dto.setPurpose(b.getPurpose());
        dto.setExperimentType(b.getExperimentType());
        dto.setCheckinTime(b.getCheckinTime());
        dto.setApprovalNote(b.getApprovalNote());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
