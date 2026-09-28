package com.militaryasset.module.transfer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransferCreateDTO {

    @NotNull(message = "Source Base ID is required")
    private Long sourceBaseId;

    @NotNull(message = "Destination Base ID is required")
    private Long destinationBaseId;

    @NotNull(message = "Equipment Type ID is required")
    private Long equipmentTypeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Transfer date is required")
    private LocalDateTime transferDate;

    private String remarks;

    public TransferCreateDTO() {}

    public TransferCreateDTO(Long sourceBaseId, Long destinationBaseId, Long equipmentTypeId, Integer quantity, LocalDateTime transferDate, String remarks) {
        this.sourceBaseId = sourceBaseId;
        this.destinationBaseId = destinationBaseId;
        this.equipmentTypeId = equipmentTypeId;
        this.quantity = quantity;
        this.transferDate = transferDate;
        this.remarks = remarks;
    }

    public Long getSourceBaseId() { return sourceBaseId; }
    public void setSourceBaseId(Long sourceBaseId) { this.sourceBaseId = sourceBaseId; }

    public Long getDestinationBaseId() { return destinationBaseId; }
    public void setDestinationBaseId(Long destinationBaseId) { this.destinationBaseId = destinationBaseId; }

    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public void setEquipmentTypeId(Long equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
