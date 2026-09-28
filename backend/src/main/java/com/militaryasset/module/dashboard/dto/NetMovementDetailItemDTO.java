package com.militaryasset.module.dashboard.dto;

import java.time.LocalDateTime;

public class NetMovementDetailItemDTO {

    private String transactionType; // "PURCHASE", "TRANSFER_IN", "TRANSFER_OUT"
    private String reference;
    private LocalDateTime transactionDate;
    private Long baseId;
    private String baseName;
    private Long relatedBaseId;
    private String relatedBaseName;
    private Long equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private Integer quantity;
    private Integer netImpact; // +quantity for purchase/transfer-in, -quantity for transfer-out
    private String recordedByUsername;
    private String remarks;

    public NetMovementDetailItemDTO() {}

    public NetMovementDetailItemDTO(String transactionType, String reference, LocalDateTime transactionDate, Long baseId, String baseName, Long relatedBaseId, String relatedBaseName, Long equipmentTypeId, String equipmentCode, String equipmentName, Integer quantity, Integer netImpact, String recordedByUsername, String remarks) {
        this.transactionType = transactionType;
        this.reference = reference;
        this.transactionDate = transactionDate;
        this.baseId = baseId;
        this.baseName = baseName;
        this.relatedBaseId = relatedBaseId;
        this.relatedBaseName = relatedBaseName;
        this.equipmentTypeId = equipmentTypeId;
        this.equipmentCode = equipmentCode;
        this.equipmentName = equipmentName;
        this.quantity = quantity;
        this.netImpact = netImpact;
        this.recordedByUsername = recordedByUsername;
        this.remarks = remarks;
    }

    public String getTransactionType() { return transactionType; }
    public String getReference() { return reference; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public Long getBaseId() { return baseId; }
    public String getBaseName() { return baseName; }
    public Long getRelatedBaseId() { return relatedBaseId; }
    public String getRelatedBaseName() { return relatedBaseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public Integer getQuantity() { return quantity; }
    public Integer getNetImpact() { return netImpact; }
    public String getRecordedByUsername() { return recordedByUsername; }
    public String getRemarks() { return remarks; }
}
