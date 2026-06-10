package com.lab.dto.request;

import com.lab.entity.Equipment;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EquipmentRequest {

    @NotBlank(message = "Equipment name is required")
    private String name;

    private String model;

    private String serialNumber;

    private UUID roomId;

    private Equipment.Category category;

    private Equipment.Status status;

    private String manufacturer;

    private LocalDate purchaseDate;

    private LocalDate lastMaintenance;

    private LocalDate nextMaintenance;

    private String notes;
}
