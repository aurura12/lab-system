package com.lab.dto.response;

import com.lab.entity.Equipment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EquipmentDTO {

    private UUID id;
    private String name;
    private String model;
    private String serialNumber;
    private UUID roomId;
    private String roomName;
    private Equipment.Category category;
    private Equipment.Status status;
    private String manufacturer;
    private LocalDate purchaseDate;
    private LocalDate lastMaintenance;
    private LocalDate nextMaintenance;
    private String notes;
    private LocalDateTime createdAt;

    public static EquipmentDTO fromEntity(Equipment equipment) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        if (equipment.getRoom() != null) {
            dto.setRoomId(equipment.getRoom().getId());
            dto.setRoomName(equipment.getRoom().getName());
        }
        dto.setCategory(equipment.getCategory());
        dto.setStatus(equipment.getStatus());
        dto.setManufacturer(equipment.getManufacturer());
        dto.setPurchaseDate(equipment.getPurchaseDate());
        dto.setLastMaintenance(equipment.getLastMaintenance());
        dto.setNextMaintenance(equipment.getNextMaintenance());
        dto.setNotes(equipment.getNotes());
        dto.setCreatedAt(equipment.getCreatedAt());
        return dto;
    }
}
