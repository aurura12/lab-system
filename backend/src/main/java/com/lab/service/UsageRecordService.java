package com.lab.service;

import com.lab.dto.request.UsageStartRequest;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.UsageRecordDTO;
import com.lab.entity.*;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PageResponse<UsageRecordDTO> getUsageRecords(UUID equipmentId, UUID userId, UUID projectId,
                                                          LocalDateTime startTime, LocalDateTime endTime,
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("loginTime").descending());
        Page<UsageRecord> recordPage;

        if (equipmentId != null && userId != null && projectId != null) {
            recordPage = usageRecordRepository.findByEquipmentIdAndUserIdAndProjectId(equipmentId, userId, projectId, pageable);
        } else if (equipmentId != null && userId != null) {
            recordPage = usageRecordRepository.findByEquipmentIdAndUserId(equipmentId, userId, pageable);
        } else if (equipmentId != null && projectId != null) {
            recordPage = usageRecordRepository.findByEquipmentIdAndProjectId(equipmentId, projectId, pageable);
        } else if (userId != null && projectId != null) {
            recordPage = usageRecordRepository.findByUserIdAndProjectId(userId, projectId, pageable);
        } else if (equipmentId != null) {
            recordPage = usageRecordRepository.findByEquipmentId(equipmentId, pageable);
        } else if (userId != null) {
            recordPage = usageRecordRepository.findByUserId(userId, pageable);
        } else if (projectId != null) {
            recordPage = usageRecordRepository.findByProjectId(projectId, pageable);
        } else {
            recordPage = usageRecordRepository.findAll(pageable);
        }

        return new PageResponse<>(
                recordPage.getContent().stream().map(UsageRecordDTO::fromEntity).toList(),
                recordPage.getTotalElements(), recordPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public List<UsageRecordDTO> getActiveUsages() {
        return usageRecordRepository.findByStatus(UsageRecord.Status.active)
                .stream().map(UsageRecordDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public UsageRecordDTO startUsage(UsageStartRequest request, UUID currentUserId) {
        // Check if equipment already has an active session
        Optional<UsageRecord> existing = usageRecordRepository.findByEquipmentIdAndStatus(
                request.getEquipmentId(), UsageRecord.Status.active);
        if (existing.isPresent()) {
            throw new BadRequestException("设备当前正在使用中");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", request.getEquipmentId()));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        UsageRecord record = new UsageRecord();
        record.setEquipment(equipment);
        record.setUser(user);
        record.setLoginTime(LocalDateTime.now());
        record.setStatus(UsageRecord.Status.active);
        record.setPurpose(request.getPurpose());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            record.setProject(project);
        }

        // Update equipment status
        equipment.setStatus(Equipment.Status.in_use);
        equipmentRepository.save(equipment);

        return UsageRecordDTO.fromEntity(usageRecordRepository.save(record));
    }

    @Transactional
    public UsageRecordDTO endUsage(UUID recordId) {
        UsageRecord record = usageRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("UsageRecord", "id", recordId));

        if (record.getStatus() == UsageRecord.Status.completed) {
            throw new BadRequestException("使用记录已完成");
        }

        record.setLogoutTime(LocalDateTime.now());
        record.setStatus(UsageRecord.Status.completed);

        // Update equipment status
        Equipment equipment = record.getEquipment();
        equipment.setStatus(Equipment.Status.available);
        equipmentRepository.save(equipment);

        return UsageRecordDTO.fromEntity(usageRecordRepository.save(record));
    }
}
