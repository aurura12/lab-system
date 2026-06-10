package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.entity.Floor;
import com.lab.entity.Lab;
import com.lab.entity.Room;
import com.lab.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @GetMapping("/labs")
    public ApiResponse<List<Lab>> getLabs() {
        return ApiResponse.success(labService.getLabs());
    }

    @PostMapping("/labs")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Lab> createLab(@RequestBody Lab lab) {
        return ApiResponse.success("Lab created", labService.createLab(lab));
    }

    @PutMapping("/labs/{id}")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Lab> updateLab(@PathVariable UUID id, @RequestBody Lab lab) {
        return ApiResponse.success("Lab updated", labService.updateLab(id, lab));
    }

    @DeleteMapping("/labs/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> deleteLab(@PathVariable UUID id) {
        labService.deleteLab(id);
        return ApiResponse.success("Lab deleted", null);
    }

    @GetMapping("/labs/{labId}/floors")
    public ApiResponse<List<Floor>> getFloors(@PathVariable UUID labId) {
        return ApiResponse.success(labService.getFloors(labId));
    }

    @PostMapping("/labs/{labId}/floors")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Floor> createFloor(@PathVariable UUID labId, @RequestBody Floor floor) {
        return ApiResponse.success("Floor created", labService.createFloor(labId, floor));
    }

    @GetMapping("/floors/{floorId}/rooms")
    public ApiResponse<List<Room>> getRooms(@PathVariable UUID floorId) {
        return ApiResponse.success(labService.getRooms(floorId));
    }

    @PostMapping("/floors/{floorId}/rooms")
    @PreAuthorize("hasAnyRole('admin', 'lab_manager')")
    public ApiResponse<Room> createRoom(@PathVariable UUID floorId, @RequestBody Room room) {
        return ApiResponse.success("Room created", labService.createRoom(floorId, room));
    }

    @GetMapping("/rooms/{roomId}")
    public ApiResponse<Map<String, Object>> getRoomDetail(@PathVariable UUID roomId) {
        return ApiResponse.success(labService.getRoomDetail(roomId));
    }
}
