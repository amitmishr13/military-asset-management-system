package com.militaryasset.module.transfer.dto;

import com.militaryasset.module.transfer.entity.Transfer;
import java.time.LocalDateTime;

public class TransferResponseDTO {

    private Long id;
    private String transferReference;
    private Long sourceBaseId;
    private String sourceBaseCode;
    private String sourceBaseName;
    private Long destinationBaseId;
    private String destinationBaseCode;
    private String destinationBaseName;
    private Long equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private String category;
    private String unitOfMeasure;
    private Integer quantity;
    private LocalDateTime transferDate;
    private String remarks;
    private Long initiatedByUserId;
    private String initiatedByUsername;
    private LocalDateTime createdAt;

    public TransferResponseDTO() {}

    public TransferResponseDTO(Transfer transfer) {
        this.id = transfer.getId();
        this.transferReference = transfer.getTransferReference();
        if (transfer.getSourceBase() != null) {
            this.sourceBaseId = transfer.getSourceBase().getId();
            this.sourceBaseCode = transfer.getSourceBase().getCode();
            this.sourceBaseName = transfer.getSourceBase().getName();
        }
        if (transfer.getDestinationBase() != null) {
            this.destinationBaseId = transfer.getDestinationBase().getId();
            this.destinationBaseCode = transfer.getDestinationBase().getCode();
            this.destinationBaseName = transfer.getDestinationBase().getName();
        }
        if (transfer.getEquipmentType() != null) {
            this.equipmentTypeId = transfer.getEquipmentType().getId();
            this.equipmentCode = transfer.getEquipmentType().getCode();
            this.equipmentName = transfer.getEquipmentType().getName();
            this.category = transfer.getEquipmentType().getCategory();
            this.unitOfMeasure = transfer.getEquipmentType().getUnitOfMeasure();
        }
        this.quantity = transfer.getQuantity();
        this.transferDate = transfer.getTransferDate();
        this.remarks = transfer.getRemarks();
        if (transfer.getInitiatedByUser() != null) {
            this.initiatedByUserId = transfer.getInitiatedByUser().getId();
            this.initiatedByUsername = transfer.getInitiatedByUser().getUsername();
        }
        this.createdAt = transfer.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getTransferReference() { return transferReference; }
    public Long getSourceBaseId() { return sourceBaseId; }
    public String getSourceBaseCode() { return sourceBaseCode; }
    public String getSourceBaseName() { return sourceBaseName; }
    public Long getDestinationBaseId() { return destinationBaseId; }
    public String getDestinationBaseCode() { return destinationBaseCode; }
    public String getDestinationBaseName() { return destinationBaseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public Integer getQuantity() { return quantity; }
    public LocalDateTime getTransferDate() { return transferDate; }
    public String getRemarks() { return remarks; }
    public Long getInitiatedByUserId() { return initiatedByUserId; }
    public String getInitiatedByUsername() { return initiatedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
