package com.lab.repository;

import com.lab.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, UUID> {

    List<StorageLocation> findByRoomIdOrderBySortOrderAsc(UUID roomId);

    List<StorageLocation> findByParentIdOrderBySortOrderAsc(UUID parentId);

    List<StorageLocation> findByParentIdIsNullAndRoomIdOrderBySortOrderAsc(UUID roomId);

    List<StorageLocation> findByLevelAndRoomId(String level, UUID roomId);
}
