package com.lab.repository;

import com.lab.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByFloorIdOrderByRoomNumber(UUID floorId);
}
