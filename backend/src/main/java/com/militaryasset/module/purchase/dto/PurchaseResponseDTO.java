package com.militaryasset.module.purchase.dto;

import com.militaryasset.module.purchase.entity.Purchase;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseResponseDTO {

    private Long id;
    private String purchaseReference;
    private Long baseId;
    private String baseCode;
    private String baseName;
    private Long equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private String category;
    private String unitOfMeasure;
    private Integer quantity;
    private BigDecimal unitCost;
    private LocalDateTime purchaseDate;
    private String supplierDetails;
    private Long recordedByUserId;
    private String recordedByUsername;
    private LocalDateTime createdAt;

    public PurchaseResponseDTO() {}

    public PurchaseResponseDTO(Purchase purchase) {
        this.id = purchase.getId();
        this.purchaseReference = purchase.getPurchaseReference();
        if (purchase.getBase() != null) {
            this.baseId = purchase.getBase().getId();
            this.baseCode = purchase.getBase().getCode();
            this.baseName = purchase.getBase().getName();
        }
        if (purchase.getEquipmentType() != null) {
            this.equipmentTypeId = purchase.getEquipmentType().getId();
            this.equipmentCode = purchase.getEquipmentType().getCode();
            this.equipmentName = purchase.getEquipmentType().getName();
            this.category = purchase.getEquipmentType().getCategory();
            this.unitOfMeasure = purchase.getEquipmentType().getUnitOfMeasure();
        }
        this.quantity = purchase.getQuantity();
        this.unitCost = purchase.getUnitCost();
        this.purchaseDate = purchase.getPurchaseDate();
        this.supplierDetails = purchase.getSupplierDetails();
        if (purchase.getRecordedByUser() != null) {
            this.recordedByUserId = purchase.getRecordedByUser().getId();
            this.recordedByUsername = purchase.getRecordedByUser().getUsername();
        }
        this.createdAt = purchase.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getPurchaseReference() { return purchaseReference; }
    public Long getBaseId() { return baseId; }
    public String getBaseCode() { return baseCode; }
    public String getBaseName() { return baseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public String getSupplierDetails() { return supplierDetails; }
    public Long getRecordedByUserId() { return recordedByUserId; }
    public String getRecordedByUsername() { return recordedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
