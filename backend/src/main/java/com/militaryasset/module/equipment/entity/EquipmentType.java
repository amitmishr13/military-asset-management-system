package com.militaryasset.module.equipment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_types")
public class EquipmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EquipmentType() {}

    public EquipmentType(Long id, String code, String name, String category, String unitOfMeasure, String description, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.description = description;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static EquipmentTypeBuilder builder() {
        return new EquipmentTypeBuilder();
    }

    public static class EquipmentTypeBuilder {
        private Long id;
        private String code;
        private String name;
        private String category;
        private String unitOfMeasure;
        private String description;
        private LocalDateTime createdAt;

        public EquipmentTypeBuilder id(Long id) { this.id = id; return this; }
        public EquipmentTypeBuilder code(String code) { this.code = code; return this; }
        public EquipmentTypeBuilder name(String name) { this.name = name; return this; }
        public EquipmentTypeBuilder category(String category) { this.category = category; return this; }
        public EquipmentTypeBuilder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public EquipmentTypeBuilder description(String description) { this.description = description; return this; }
        public EquipmentTypeBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public EquipmentType build() {
            return new EquipmentType(id, code, name, category, unitOfMeasure, description, createdAt);
        }
    }
}
