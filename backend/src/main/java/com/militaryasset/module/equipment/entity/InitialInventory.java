package com.militaryasset.module.equipment.entity;

import com.militaryasset.module.base.entity.Base;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "initial_inventory", uniqueConstraints = {
    @UniqueConstraint(name = "uk_base_equipment", columnNames = {"base_id", "equipment_type_id"})
})
public class InitialInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    public InitialInventory() {}

    public InitialInventory(Long id, Base base, EquipmentType equipmentType, Integer initialQuantity, LocalDate asOfDate) {
        this.id = id;
        this.base = base;
        this.equipmentType = equipmentType;
        this.initialQuantity = initialQuantity;
        this.asOfDate = asOfDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Base getBase() { return base; }
    public void setBase(Base base) { this.base = base; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public Integer getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(Integer initialQuantity) { this.initialQuantity = initialQuantity; }

    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; }

    public static InitialInventoryBuilder builder() {
        return new InitialInventoryBuilder();
    }

    public static class InitialInventoryBuilder {
        private Long id;
        private Base base;
        private EquipmentType equipmentType;
        private Integer initialQuantity;
        private LocalDate asOfDate;

        public InitialInventoryBuilder id(Long id) { this.id = id; return this; }
        public InitialInventoryBuilder base(Base base) { this.base = base; return this; }
        public InitialInventoryBuilder equipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; return this; }
        public InitialInventoryBuilder initialQuantity(Integer initialQuantity) { this.initialQuantity = initialQuantity; return this; }
        public InitialInventoryBuilder asOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; return this; }

        public InitialInventory build() {
            return new InitialInventory(id, base, equipmentType, initialQuantity, asOfDate);
        }
    }
}
