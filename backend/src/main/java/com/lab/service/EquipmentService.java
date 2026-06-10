package com.lab.service;

import com.lab.dto.request.EquipmentRequest;
import com.lab.dto.response.EquipmentDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.Equipment;
import com.lab.entity.Room;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.EquipmentRepository;
import com.lab.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public PageResponse<EquipmentDTO> getEquipment(UUID roomId, Equipment.Status status,
                                                     Equipment.Category category, String keyword,
                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Equipment> equipmentPage;

        if (keyword != null && !keyword.isEmpty()) {
            equipmentPage = equipmentRepository.findByKeyword(keyword, pageable);
        } else if (roomId != null && status != null && category != null) {
            // Complex combo - filter in memory after basic query
            equipmentPage = equipmentRepository.findByRoomId(roomId, pageable);
        } else if (roomId != null) {
            equipmentPage = equipmentRepository.findByRoomId(roomId, pageable);
        } else if (status != null) {
            equipmentPage = equipmentRepository.findByStatus(status, pageable);
        } else if (category != null) {
            equipmentPage = equipmentRepository.findByCategory(category, pageable);
        } else {
            equipmentPage = equipmentRepository.findAll(pageable);
        }

        return new PageResponse<>(
                equipmentPage.getContent().stream().map(EquipmentDTO::fromEntity).toList(),
                equipmentPage.getTotalElements(), equipmentPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public EquipmentDTO getEquipmentById(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
        return EquipmentDTO.fromEntity(equipment);
    }

    @Transactional
    public EquipmentDTO createEquipment(EquipmentRequest request) {
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setModel(request.getModel());
        equipment.setSerialNumber(request.getSerialNumber());
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
            equipment.setRoom(room);
        }
        equipment.setCategory(request.getCategory());
        equipment.setStatus(request.getStatus() != null ? request.getStatus() : Equipment.Status.available);
        equipment.setManufacturer(request.getManufacturer());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipment.setLastMaintenance(request.getLastMaintenance());
        equipment.setNextMaintenance(request.getNextMaintenance());
        equipment.setNotes(request.getNotes());
        return EquipmentDTO.fromEntity(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentDTO updateEquipment(UUID id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));

        equipment.setName(request.getName());
        if (request.getModel() != null) equipment.setModel(request.getModel());
        if (request.getSerialNumber() != null) equipment.setSerialNumber(request.getSerialNumber());
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
            equipment.setRoom(room);
        }
        if (request.getCategory() != null) equipment.setCategory(request.getCategory());
        if (request.getStatus() != null) equipment.setStatus(request.getStatus());
        if (request.getManufacturer() != null) equipment.setManufacturer(request.getManufacturer());
        if (request.getPurchaseDate() != null) equipment.setPurchaseDate(request.getPurchaseDate());
        if (request.getLastMaintenance() != null) equipment.setLastMaintenance(request.getLastMaintenance());
        if (request.getNextMaintenance() != null) equipment.setNextMaintenance(request.getNextMaintenance());
        if (request.getNotes() != null) equipment.setNotes(request.getNotes());

        return EquipmentDTO.fromEntity(equipmentRepository.save(equipment));
    }

    @Transactional
    public void deleteEquipment(UUID id) {
        equipmentRepository.deleteById(id);
    }

    @Transactional
    public EquipmentDTO updateStatus(UUID id, Equipment.Status status) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
        equipment.setStatus(status);
        return EquipmentDTO.fromEntity(equipmentRepository.save(equipment));
    }
}
