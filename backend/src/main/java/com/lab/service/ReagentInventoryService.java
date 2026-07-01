package com.lab.service;

import com.lab.dto.request.BatchUseItem;
import com.lab.dto.request.BatchUseRequest;
import com.lab.dto.request.ReagentInventoryRequest;
import com.lab.dto.request.ReagentUseRequest;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentInventoryDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReagentInventoryService {

    private final ReagentInventoryRepository inventoryRepository;
    private final ReagentCategoryRepository categoryRepository;
    private final StorageLocationRepository locationRepository;
    private final ReagentTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReagentInventoryDTO> getInventory(String keyword, String status,
                                                           String alertLevel, UUID categoryId,
                                                           UUID locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        Page<ReagentInventory> inventoryPage;

        if (keyword != null && !keyword.isEmpty()) {
            inventoryPage = inventoryRepository.findByKeyword(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            inventoryPage = inventoryRepository.findByStatus(status, pageable);
        } else if (alertLevel != null && !alertLevel.isEmpty()) {
            inventoryPage = inventoryRepository.findByAlertLevel(alertLevel, pageable);
        } else if (categoryId != null) {
            inventoryPage = inventoryRepository.findByCategoryId(categoryId, pageable);
        } else if (locationId != null) {
            inventoryPage = inventoryRepository.findByLocationId(locationId, pageable);
        } else {
            inventoryPage = inventoryRepository.findAll(pageable);
        }

        return new PageResponse<>(
                inventoryPage.getContent().stream().map(ReagentInventoryDTO::fromEntity).toList(),
                inventoryPage.getTotalElements(), inventoryPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public ReagentInventoryDTO getById(UUID id) {
        return ReagentInventoryDTO.fromEntity(findInventory(id));
    }

    @Transactional(readOnly = true)
    public List<ReagentInventoryDTO> getExpiring() {
        List<String> alertLevels = List.of("warning_days", "warning_urgent", "expired");
        return inventoryRepository.findByAlertLevelIn(alertLevels).stream()
                .map(ReagentInventoryDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReagentInventoryDTO> getOutOfStock() {
        return inventoryRepository.findLowStock().stream()
                .map(ReagentInventoryDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReagentInventoryDTO> recommendFefo(UUID categoryId) {
        List<String> excluded = List.of("expired", "disposed");
        return inventoryRepository
                .findByCategoryIdAndStatusNotInOrderByExpiryDateAsc(categoryId, excluded)
                .stream()
                .filter(i -> i.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(ReagentInventoryDTO::fromEntity)
                .toList();
    }

    @Transactional
    public ReagentInventoryDTO inbound(ReagentInventoryRequest request, UUID operatorId) {
        ReagentCategory category = categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("ReagentCategory", "id", request.getCategoryId()));

        if (request.getBarcode() != null && !request.getBarcode().isEmpty()
                && inventoryRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("条码已存在: " + request.getBarcode());
        }

        ReagentInventory inventory = new ReagentInventory();
        inventory.setCategory(category);
        inventory.setBarcode(request.getBarcode() != null && !request.getBarcode().isEmpty()
                ? request.getBarcode() : generateBarcode());
        inventory.setBatchNo(request.getBatchNo() != null ? request.getBatchNo() : generateBatchNo());
        inventory.setTotalQuantity(request.getTotalQuantity());
        inventory.setRemainingQuantity(request.getTotalQuantity());
        inventory.setUnit(request.getUnit() != null ? request.getUnit() : category.getUnit());
        inventory.setManufactureDate(request.getManufactureDate());
        inventory.setExpiryDate(request.getExpiryDate());
        inventory.setReceivedDate(LocalDate.now());
        inventory.setSupplier(request.getSupplier());
        inventory.setStatus("unopened");
        inventory.setStorageConditions(request.getStorageConditions());

        if (request.getLocationId() != null && !request.getLocationId().isEmpty()) {
            inventory.setLocation(locationRepository.findById(UUID.fromString(request.getLocationId())).orElse(null));
        }

        // Calculate initial alert level
        inventory.setAlertLevel(calculateAlertLevel(request.getExpiryDate(), "unopened"));

        ReagentInventory saved = inventoryRepository.save(inventory);

        // Create inbound transaction record
        ReagentTransaction transaction = new ReagentTransaction();
        transaction.setInventory(saved);
        transaction.setType("inbound");
        transaction.setQuantity(request.getTotalQuantity());
        transaction.setOperator(userRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", operatorId)));
        transaction.setPurpose("入库");
        transactionRepository.save(transaction);

        return ReagentInventoryDTO.fromEntity(saved);
    }

    @Transactional
    public ReagentInventoryDTO useSingle(UUID id, ReagentUseRequest request, UUID operatorId) {
        ReagentInventory inventory = findInventory(id);

        if ("expired".equals(inventory.getStatus()) || "disposed".equals(inventory.getStatus())) {
            throw new BadRequestException("Cannot use reagent that is " + inventory.getStatus());
        }

        BigDecimal quantity = request.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        if (quantity.compareTo(inventory.getRemainingQuantity()) > 0) {
            throw new BadRequestException("Insufficient remaining quantity. Available: "
                    + inventory.getRemainingQuantity() + " " + inventory.getUnit());
        }

        // Deduct quantity
        BigDecimal newRemaining = inventory.getRemainingQuantity().subtract(quantity);
        inventory.setRemainingQuantity(newRemaining);

        // Update status based on consumption
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            inventory.setStatus("disposed");
        } else if ("unopened".equals(inventory.getStatus())) {
            inventory.setStatus("opened");
            inventory.setOpenDate(LocalDate.now());
        }

        inventoryRepository.save(inventory);

        // Create outbound transaction
        ReagentTransaction transaction = new ReagentTransaction();
        transaction.setInventory(inventory);
        transaction.setType("outbound");
        transaction.setQuantity(quantity);
        transaction.setOperator(userRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", operatorId)));
        transaction.setPurpose(request.getPurpose());

        if (request.getProjectId() != null && !request.getProjectId().isEmpty()) {
            transaction.setProject(projectRepository.findById(UUID.fromString(request.getProjectId())).orElse(null));
        }

        transactionRepository.save(transaction);

        return ReagentInventoryDTO.fromEntity(inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReagentInventory", "id", id)));
    }

    @Transactional
    public List<ReagentInventoryDTO> batchUse(BatchUseRequest request, UUID operatorId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Batch use items cannot be empty");
        }

        List<ReagentInventoryDTO> results = new ArrayList<>();
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", operatorId));
        Project project = null;
        if (request.getProjectId() != null && !request.getProjectId().isEmpty()) {
            project = projectRepository.findById(UUID.fromString(request.getProjectId())).orElse(null);
        }

        for (BatchUseItem item : request.getItems()) {
            ReagentInventory inventory = findInventory(UUID.fromString(item.getInventoryId()));

            if ("expired".equals(inventory.getStatus()) || "disposed".equals(inventory.getStatus())) {
                throw new BadRequestException("Cannot use reagent " + inventory.getBarcode()
                        + " that is " + inventory.getStatus());
            }

            BigDecimal qty = item.getQuantity();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Quantity must be positive for item: " + item.getInventoryId());
            }

            if (qty.compareTo(inventory.getRemainingQuantity()) > 0) {
                throw new BadRequestException("Insufficient remaining quantity for "
                        + inventory.getBarcode() + ". Available: "
                        + inventory.getRemainingQuantity());
            }

            BigDecimal newRemaining = inventory.getRemainingQuantity().subtract(qty);
            inventory.setRemainingQuantity(newRemaining);

            if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                inventory.setStatus("disposed");
            } else if ("unopened".equals(inventory.getStatus())) {
                inventory.setStatus("opened");
                inventory.setOpenDate(LocalDate.now());
            }

            inventoryRepository.save(inventory);

            ReagentTransaction transaction = new ReagentTransaction();
            transaction.setInventory(inventory);
            transaction.setType("outbound");
            transaction.setQuantity(qty);
            transaction.setOperator(operator);
            transaction.setPurpose(request.getPurpose());
            transaction.setProject(project);
            transactionRepository.save(transaction);

            results.add(ReagentInventoryDTO.fromEntity(inventory));
        }

        return results;
    }

    @Transactional
    public ReagentInventoryDTO update(UUID id, ReagentInventoryRequest request) {
        ReagentInventory inventory = findInventory(id);

        if (request.getBarcode() != null && !request.getBarcode().isEmpty()
                && !request.getBarcode().equals(inventory.getBarcode())
                && inventoryRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Barcode already exists: " + request.getBarcode());
        }

        if (request.getBarcode() != null) inventory.setBarcode(request.getBarcode());
        if (request.getBatchNo() != null) inventory.setBatchNo(request.getBatchNo());
        if (request.getSupplier() != null) inventory.setSupplier(request.getSupplier());
        if (request.getStorageConditions() != null) inventory.setStorageConditions(request.getStorageConditions());

        if (request.getLocationId() != null) {
            if (request.getLocationId().isEmpty()) {
                inventory.setLocation(null);
            } else {
                inventory.setLocation(locationRepository.findById(UUID.fromString(request.getLocationId())).orElse(null));
            }
        }

        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
            inventory.setAlertLevel(calculateAlertLevel(request.getExpiryDate(), inventory.getStatus()));
        }

        return ReagentInventoryDTO.fromEntity(inventoryRepository.save(inventory));
    }

    @Transactional
    public void delete(UUID id) {
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public void updateAlertLevels() {
        List<ReagentInventory> all = inventoryRepository.findAll();
        List<ReagentInventory> toUpdate = new ArrayList<>();
        for (ReagentInventory item : all) {
            String newLevel = calculateAlertLevel(item.getExpiryDate(), item.getStatus());
            if (!newLevel.equals(item.getAlertLevel())) {
                item.setAlertLevel(newLevel);
                toUpdate.add(item);
            }
        }
        if (!toUpdate.isEmpty()) {
            inventoryRepository.saveAll(toUpdate);
        }
    }

    private String calculateAlertLevel(LocalDate expiryDate, String status) {
        if ("expired".equals(status) || "disposed".equals(status)) {
            return "expired";
        }
        if (expiryDate == null) return "normal";

        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (daysUntilExpiry < 0) return "expired";
        if (daysUntilExpiry <= 30) return "warning_urgent";
        if (daysUntilExpiry <= 90) return "warning_days";
        return "normal";
    }

    private String generateBatchNo() {
        return "B" + LocalDate.now().toString().replace("-", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateBarcode() {
        return "R" + LocalDate.now().toString().replace("-", "")
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private ReagentInventory findInventory(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReagentInventory", "id", id));
    }
}
