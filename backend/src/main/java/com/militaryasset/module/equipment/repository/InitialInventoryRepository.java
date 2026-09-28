package com.militaryasset.module.equipment.repository;

import com.militaryasset.module.equipment.entity.InitialInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InitialInventoryRepository extends JpaRepository<InitialInventory, Long> {
    Optional<InitialInventory> findByBaseIdAndEquipmentTypeId(Long baseId, Long equipmentTypeId);
    List<InitialInventory> findByBaseId(Long baseId);
}
