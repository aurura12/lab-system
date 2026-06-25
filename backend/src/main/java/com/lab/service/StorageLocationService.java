package com.lab.service;

import com.lab.dto.request.StorageLocationRequest;
import com.lab.dto.response.StorageLocationDTO;
import com.lab.entity.Room;
import com.lab.entity.StorageLocation;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.RoomRepository;
import com.lab.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageLocationService {

    private final StorageLocationRepository locationRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<StorageLocationDTO> getTreeByRoomId(UUID roomId) {
        List<StorageLocation> roots = locationRepository.findByParentIdIsNullAndRoomIdOrderBySortOrderAsc(roomId);
        return roots.stream()
                .map(this::toTreeDTO)
                .toList();
    }

    private StorageLocationDTO toTreeDTO(StorageLocation node) {
        StorageLocationDTO dto = StorageLocationDTO.fromEntity(node);
        List<StorageLocation> children = locationRepository.findByParentIdOrderBySortOrderAsc(node.getId());
        dto.setChildren(children.stream().map(this::toTreeDTO).toList());
        return dto;
    }

    @Transactional(readOnly = true)
    public StorageLocationDTO getById(UUID id) {
        return StorageLocationDTO.fromEntity(findLocation(id));
    }

    @Transactional
    public StorageLocationDTO create(StorageLocationRequest request) {
        StorageLocation location = new StorageLocation();
        location.setLevel(request.getLevel());
        location.setCode(request.getCode());
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        Room room = roomRepository.findById(UUID.fromString(request.getRoomId()))
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
        location.setRoom(room);

        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            StorageLocation parent = findLocation(UUID.fromString(request.getParentId()));
            location.setParent(parent);
            location.setPath(parent.getPath() + "/" + request.getCode());
        } else {
            location.setPath(request.getCode());
        }

        return StorageLocationDTO.fromEntity(locationRepository.save(location));
    }

    @Transactional
    public StorageLocationDTO update(UUID id, StorageLocationRequest request) {
        StorageLocation location = findLocation(id);

        if (request.getCode() != null) location.setCode(request.getCode());
        if (request.getName() != null) location.setName(request.getName());
        if (request.getDescription() != null) location.setDescription(request.getDescription());
        if (request.getSortOrder() != null) location.setSortOrder(request.getSortOrder());

        if (request.getCode() != null || request.getParentId() != null) {
            if (location.getParent() != null) {
                location.setPath(location.getParent().getPath() + "/" + location.getCode());
            } else {
                location.setPath(location.getCode());
            }
        }

        return StorageLocationDTO.fromEntity(locationRepository.save(location));
    }

    @Transactional
    public void delete(UUID id) {
        StorageLocation location = findLocation(id);
        locationRepository.delete(location);
    }

    private StorageLocation findLocation(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", id));
    }
}
