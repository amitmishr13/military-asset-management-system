package com.militaryasset.module.purchase.entity;

import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.equipment.entity.EquipmentType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases", indexes = {
    @Index(name = "idx_purchases_base_equip_date", columnList = "base_id, equipment_type_id, purchase_date")
})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_reference", nullable = false, unique = true, length = 50)
    private String purchaseReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "supplier_details", length = 255)
    private String supplierDetails;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Purchase() {}

    public Purchase(Long id, String purchaseReference, Base base, EquipmentType equipmentType, Integer quantity, BigDecimal unitCost, LocalDateTime purchaseDate, String supplierDetails, User recordedByUser, LocalDateTime createdAt) {
        this.id = id;
        this.purchaseReference = purchaseReference;
        this.base = base;
        this.equipmentType = equipmentType;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.purchaseDate = purchaseDate;
        this.supplierDetails = supplierDetails;
        this.recordedByUser = recordedByUser;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPurchaseReference() { return purchaseReference; }
    public void setPurchaseReference(String purchaseReference) { this.purchaseReference = purchaseReference; }

    public Base getBase() { return base; }
    public void setBase(Base base) { this.base = base; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getSupplierDetails() { return supplierDetails; }
    public void setSupplierDetails(String supplierDetails) { this.supplierDetails = supplierDetails; }

    public User getRecordedByUser() { return recordedByUser; }
    public void setRecordedByUser(User recordedByUser) { this.recordedByUser = recordedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static PurchaseBuilder builder() {
        return new PurchaseBuilder();
    }

    public static class PurchaseBuilder {
        private Long id;
        private String purchaseReference;
        private Base base;
        private EquipmentType equipmentType;
        private Integer quantity;
        private BigDecimal unitCost;
        private LocalDateTime purchaseDate;
        private String supplierDetails;
        private User recordedByUser;
        private LocalDateTime createdAt;

        public PurchaseBuilder id(Long id) { this.id = id; return this; }
        public PurchaseBuilder purchaseReference(String purchaseReference) { this.purchaseReference = purchaseReference; return this; }
        public PurchaseBuilder base(Base base) { this.base = base; return this; }
        public PurchaseBuilder equipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; return this; }
        public PurchaseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public PurchaseBuilder unitCost(BigDecimal unitCost) { this.unitCost = unitCost; return this; }
        public PurchaseBuilder purchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; return this; }
        public PurchaseBuilder supplierDetails(String supplierDetails) { this.supplierDetails = supplierDetails; return this; }
        public PurchaseBuilder recordedByUser(User recordedByUser) { this.recordedByUser = recordedByUser; return this; }
        public PurchaseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Purchase build() {
            return new Purchase(id, purchaseReference, base, equipmentType, quantity, unitCost, purchaseDate, supplierDetails, recordedByUser, createdAt);
        }
    }
}
