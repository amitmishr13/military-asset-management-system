package com.militaryasset.module.expenditure.repository;

import com.militaryasset.module.expenditure.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long>, JpaSpecificationExecutor<Expenditure> {
    List<Expenditure> findByBaseId(Long baseId);
    List<Expenditure> findByAssignmentId(Long assignmentId);
    List<Expenditure> findByBaseIdAndEquipmentTypeId(Long baseId, Long equipmentTypeId);
}
