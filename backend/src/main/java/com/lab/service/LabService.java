package com.lab.service;

import com.lab.entity.*;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    public List<Lab> getLabs() {
        return labRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Lab getLabById(UUID id) {
        return labRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", "id", id));
    }

    @Transactional
    public Lab createLab(Lab lab) {
        return labRepository.save(lab);
    }

    @Transactional
    public Lab updateLab(UUID id, Lab updated) {
        Lab lab = getLabById(id);
        lab.setName(updated.getName());
        lab.setDescription(updated.getDescription());
        lab.setAddress(updated.getAddress());
        return labRepository.save(lab);
    }

    @Transactional
    public void deleteLab(UUID id) {
        labRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Floor> getFloors(UUID labId) {
        getLabById(labId); // validate lab exists
        return floorRepository.findByLabIdOrderByFloorNumber(labId);
    }

    @Transactional
    public Floor createFloor(UUID labId, Floor floor) {
        Lab lab = getLabById(labId);
        floor.setLab(lab);
        return floorRepository.save(floor);
    }

    @Transactional(readOnly = true)
    public List<Room> getRooms(UUID floorId) {
        floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", floorId));
        return roomRepository.findByFloorIdOrderByRoomNumber(floorId);
    }

    @Transactional
    public Room createRoom(UUID floorId, Room room) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", floorId));
        room.setFloor(floor);
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoomDetail(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        List<Equipment> equipment = equipmentRepository.findByRoomId(roomId);
        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        result.put("equipment", equipment);
        return result;
    }
}
