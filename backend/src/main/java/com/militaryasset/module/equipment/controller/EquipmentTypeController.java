package com.militaryasset.module.equipment.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.equipment.dto.EquipmentTypeDTO;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment-types")
public class EquipmentTypeController {

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')")
    public ResponseEntity<ApiResponse<List<EquipmentTypeDTO>>> getAllEquipmentTypes() {
        List<EquipmentTypeDTO> list = equipmentTypeRepository.findAll().stream()
                .map(EquipmentTypeDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Equipment types retrieved successfully", list));
    }
}
