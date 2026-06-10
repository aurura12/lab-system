package com.lab.repository;

import com.lab.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID> {

    List<Floor> findByLabIdOrderByFloorNumber(UUID labId);
}
