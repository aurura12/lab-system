package com.lab.dto.response;

import com.lab.entity.StorageLocation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class StorageLocationDTO {
    private UUID id;
    private UUID parentId;
    private UUID roomId;
    private String level;
    private String code;
    private String name;
    private String path;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private List<StorageLocationDTO> children = new ArrayList<>();

    public static StorageLocationDTO fromEntity(StorageLocation location) {
        StorageLocationDTO dto = new StorageLocationDTO();
        dto.setId(location.getId());
        if (location.getParent() != null) {
            dto.setParentId(location.getParent().getId());
        }
        if (location.getRoom() != null) {
            dto.setRoomId(location.getRoom().getId());
        }
        dto.setLevel(location.getLevel());
        dto.setCode(location.getCode());
        dto.setName(location.getName());
        dto.setPath(location.getPath());
        dto.setDescription(location.getDescription());
        dto.setSortOrder(location.getSortOrder());
        dto.setCreatedAt(location.getCreatedAt());
        return dto;
    }
}
