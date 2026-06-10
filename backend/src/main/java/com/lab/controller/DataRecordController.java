package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.DataRecordDTO;
import com.lab.dto.response.PageResponse;
import com.lab.service.DataRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class DataRecordController {

    private final DataRecordService dataRecordService;

    @GetMapping
    public ApiResponse<PageResponse<DataRecordDTO>> getDataRecords(
            @RequestParam(required = false) UUID equipmentId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(dataRecordService.getDataRecords(equipmentId, userId, projectId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<DataRecordDTO> getDataRecordById(@PathVariable UUID id) {
        return ApiResponse.success(dataRecordService.getDataRecordById(id));
    }

    @PostMapping("/upload")
    public ApiResponse<DataRecordDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID equipmentId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID usageRecordId,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String description) throws IOException {
        DataRecordDTO record = dataRecordService.uploadFile(
                file, equipmentId, userId, projectId, usageRecordId, dataType, description);
        return ApiResponse.success("文件已上传", record);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        Path filePath = dataRecordService.getFilePath(id);
        Resource resource = new FileSystemResource(filePath.toFile());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDataRecord(@PathVariable UUID id) {
        dataRecordService.deleteDataRecord(id);
        return ApiResponse.success("数据记录已删除", null);
    }
}
