package com.lab.service;

import com.lab.dto.response.DataRecordDTO;
import com.lab.dto.response.PageResponse;
import com.lab.entity.*;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataRecordService {

    private final DataRecordRepository dataRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UsageRecordRepository usageRecordRepository;

    @Value("${app.file.upload-dir:./data-storage}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public PageResponse<DataRecordDTO> getDataRecords(UUID equipmentId, UUID userId, UUID projectId,
                                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DataRecord> recordPage;

        if (equipmentId != null && userId != null && projectId != null) {
            recordPage = dataRecordRepository.findByEquipmentIdAndUserIdAndProjectId(equipmentId, userId, projectId, pageable);
        } else if (equipmentId != null && userId != null) {
            recordPage = dataRecordRepository.findByEquipmentIdAndUserId(equipmentId, userId, pageable);
        } else if (equipmentId != null && projectId != null) {
            recordPage = dataRecordRepository.findByEquipmentIdAndProjectId(equipmentId, projectId, pageable);
        } else if (userId != null && projectId != null) {
            recordPage = dataRecordRepository.findByUserIdAndProjectId(userId, projectId, pageable);
        } else if (equipmentId != null) {
            recordPage = dataRecordRepository.findByEquipmentId(equipmentId, pageable);
        } else if (userId != null) {
            recordPage = dataRecordRepository.findByUserId(userId, pageable);
        } else if (projectId != null) {
            recordPage = dataRecordRepository.findByProjectId(projectId, pageable);
        } else {
            recordPage = dataRecordRepository.findAll(pageable);
        }

        return new PageResponse<>(
                recordPage.getContent().stream().map(DataRecordDTO::fromEntity).toList(),
                recordPage.getTotalElements(), recordPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public DataRecordDTO getDataRecordById(UUID id) {
        DataRecord record = dataRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataRecord", "id", id));
        return DataRecordDTO.fromEntity(record);
    }

    @Transactional
    public DataRecordDTO uploadFile(MultipartFile file, UUID equipmentId, UUID userId,
                                      UUID projectId, UUID usageRecordId,
                                      String dataType, String description) throws IOException {
        // Save file to disk
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // Create record
        DataRecord record = new DataRecord();
        record.setFileName(file.getOriginalFilename());
        record.setFilePath(fileName);
        record.setFileSize(file.getSize());
        record.setDataType(dataType);
        record.setDescription(description);

        if (equipmentId != null) {
            Equipment equipment = equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
            record.setEquipment(equipment);
        }
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            record.setUser(user);
        }
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            record.setProject(project);
        }
        if (usageRecordId != null) {
            UsageRecord usageRecord = usageRecordRepository.findById(usageRecordId)
                    .orElseThrow(() -> new ResourceNotFoundException("UsageRecord", "id", usageRecordId));
            record.setUsageRecord(usageRecord);
        }

        return DataRecordDTO.fromEntity(dataRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public Path getFilePath(UUID id) {
        DataRecord record = dataRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataRecord", "id", id));
        return Paths.get(uploadDir).resolve(record.getFilePath());
    }

    @Transactional
    public void deleteDataRecord(UUID id) {
        dataRecordRepository.deleteById(id);
    }
}
