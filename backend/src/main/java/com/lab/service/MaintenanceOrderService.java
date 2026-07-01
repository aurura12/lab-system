package com.lab.service;

import com.lab.dto.request.MaintenanceOrderRequest;
import com.lab.dto.response.MaintenanceOrderDTO;
import com.lab.entity.MaintenanceOrder;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.EquipmentRepository;
import com.lab.repository.MaintenanceOrderRepository;
import com.lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceOrderService {

    private final MaintenanceOrderRepository orderRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<MaintenanceOrderDTO> getOrders(String equipmentId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        UUID eqId = equipmentId != null ? UUID.fromString(equipmentId) : null;
        return orderRepository.findByFilters(eqId, status, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceOrderDTO> getUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(7);
        return orderRepository.findUpcoming(today, deadline).stream().map(this::toDTO).toList();
    }

    @Transactional
    public MaintenanceOrderDTO create(MaintenanceOrderRequest request, UUID creatorId) {
        MaintenanceOrder order = new MaintenanceOrder();
        order.setEquipment(equipmentRepository.findById(UUID.fromString(request.getEquipmentId()))
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", request.getEquipmentId())));
        order.setTitle(request.getTitle());
        order.setType(request.getType() != null ? request.getType() : "routine");
        order.setPriority(request.getPriority() != null ? request.getPriority() : "medium");
        order.setStatus("pending");
        order.setCreator(userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId)));
        order.setDescription(request.getDescription());
        order.setScheduledDate(request.getScheduledDate());
        if (request.getAssigneeId() != null && !request.getAssigneeId().isEmpty()) {
            order.setAssignee(userRepository.findById(UUID.fromString(request.getAssigneeId())).orElse(null));
        }
        return toDTO(orderRepository.save(order));
    }

    @Transactional
    public MaintenanceOrderDTO update(UUID id, MaintenanceOrderRequest request) {
        MaintenanceOrder order = findOrder(id);
        if (request.getTitle() != null) order.setTitle(request.getTitle());
        if (request.getType() != null) order.setType(request.getType());
        if (request.getPriority() != null) order.setPriority(request.getPriority());
        if (request.getStatus() != null) order.setStatus(request.getStatus());
        if (request.getDescription() != null) order.setDescription(request.getDescription());
        if (request.getResolution() != null) order.setResolution(request.getResolution());
        if (request.getScheduledDate() != null) order.setScheduledDate(request.getScheduledDate());
        if (request.getAssigneeId() != null && !request.getAssigneeId().isEmpty()) {
            order.setAssignee(userRepository.findById(UUID.fromString(request.getAssigneeId())).orElse(null));
        }
        if ("completed".equals(request.getStatus()) && request.getCompletedDate() != null) {
            order.setCompletedDate(request.getCompletedDate());
        }
        if (request.getCost() != null) order.setCost(request.getCost());
        return toDTO(orderRepository.save(order));
    }

    @Transactional
    public MaintenanceOrderDTO complete(UUID id, String resolution) {
        MaintenanceOrder order = findOrder(id);
        order.setStatus("completed");
        order.setCompletedDate(LocalDate.now());
        if (resolution != null) order.setResolution(resolution);

        // 更新设备的维护日期
        if (order.getEquipment() != null) {
            var equipment = order.getEquipment();
            equipment.setLastMaintenance(LocalDate.now());
            equipment.setNextMaintenance(LocalDate.now().plusDays(90));
            equipmentRepository.save(equipment);
        }

        return toDTO(orderRepository.save(order));
    }

    @Transactional
    public void delete(UUID id) {
        orderRepository.deleteById(id);
    }

    private MaintenanceOrder findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceOrder", "id", id));
    }

    private MaintenanceOrderDTO toDTO(MaintenanceOrder o) {
        MaintenanceOrderDTO dto = new MaintenanceOrderDTO();
        dto.setId(o.getId());
        dto.setEquipmentId(o.getEquipmentId());
        if (o.getEquipment() != null) dto.setEquipmentName(o.getEquipment().getName());
        dto.setTitle(o.getTitle());
        dto.setType(o.getType());
        dto.setPriority(o.getPriority());
        dto.setStatus(o.getStatus());
        dto.setAssigneeId(o.getAssigneeId());
        if (o.getAssignee() != null) dto.setAssigneeName(o.getAssignee().getRealName());
        if (o.getCreator() != null) {
            dto.setCreatorId(o.getCreator().getId());
            dto.setCreatorName(o.getCreator().getRealName());
        }
        dto.setDescription(o.getDescription());
        dto.setResolution(o.getResolution());
        dto.setScheduledDate(o.getScheduledDate());
        dto.setCompletedDate(o.getCompletedDate());
        dto.setCost(o.getCost());
        dto.setCreatedAt(o.getCreatedAt().toLocalDate());
        return dto;
    }
}
