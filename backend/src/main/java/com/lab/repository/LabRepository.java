package com.lab.repository;

import com.lab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabRepository extends JpaRepository<Lab, UUID> {
}
