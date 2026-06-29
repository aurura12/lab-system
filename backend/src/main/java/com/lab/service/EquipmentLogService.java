package com.lab.service;

import com.lab.dto.request.EquipmentLogRequest;
import com.lab.dto.response.EquipmentLogDTO;
import com.lab.entity.Equipment;
import com.lab.entity.EquipmentLog;
import com.lab.entity.User;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.EquipmentLogRepository;
import com.lab.repository.EquipmentRepository;
import com.lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentLogService {

    private final EquipmentLogRepository logRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<EquipmentLogDTO> getLogs(String equipmentId, String logType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        UUID eqId = equipmentId != null ? UUID.fromString(equipmentId) : null;
        return logRepository.findByFilters(eqId, logType, pageable).map(this::toDTO);
    }

    @Transactional
    public EquipmentLogDTO create(EquipmentLogRequest request, UUID userId) {
        EquipmentLog log = new EquipmentLog();
        log.setEquipment(equipmentRepository.findById(UUID.fromString(request.getEquipmentId()))
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", request.getEquipmentId())));
        log.setLogType(request.getLogType());
        log.setDescription(request.getDescription());
        log.setRecordedBy(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
        log.setAnomalyFlag(request.getAnomalyFlag() != null && request.getAnomalyFlag());
        return toDTO(logRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<EquipmentLogDTO> getRecentAnomalies(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return logRepository.findRecentAnomalies(since).stream().map(this::toDTO).toList();
    }

    private EquipmentLogDTO toDTO(EquipmentLog log) {
        EquipmentLogDTO dto = new EquipmentLogDTO();
        dto.setId(log.getId());
        dto.setEquipmentId(log.getEquipmentId());
        if (log.getEquipment() != null) dto.setEquipmentName(log.getEquipment().getName());
        dto.setLogType(log.getLogType());
        dto.setDescription(log.getDescription());
        dto.setRecordedById(log.getRecordedById());
        if (log.getRecordedBy() != null) dto.setRecordedByName(log.getRecordedBy().getRealName());
        dto.setAnomalyFlag(log.getAnomalyFlag());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
