package com.militaryasset.module.equipment.dto;

import com.militaryasset.module.equipment.entity.EquipmentType;

public class EquipmentTypeDTO {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String unitOfMeasure;
    private String description;

    public EquipmentTypeDTO() {}

    public EquipmentTypeDTO(EquipmentType equipmentType) {
        this.id = equipmentType.getId();
        this.code = equipmentType.getCode();
        this.name = equipmentType.getName();
        this.category = equipmentType.getCategory();
        this.unitOfMeasure = equipmentType.getUnitOfMeasure();
        this.description = equipmentType.getDescription();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public String getDescription() { return description; }
}
