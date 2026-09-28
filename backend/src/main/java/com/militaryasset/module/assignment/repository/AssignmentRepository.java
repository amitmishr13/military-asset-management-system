package com.militaryasset.module.assignment.repository;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long>, JpaSpecificationExecutor<Assignment> {
    List<Assignment> findByBaseId(Long baseId);
    List<Assignment> findByBaseIdAndStatus(Long baseId, AssignmentStatus status);
    List<Assignment> findByBaseIdAndEquipmentTypeId(Long baseId, Long equipmentTypeId);
    List<Assignment> findByBaseIdAndEquipmentTypeIdAndStatus(Long baseId, Long equipmentTypeId, AssignmentStatus status);
}
