package com.militaryasset.module.expenditure.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ExpenditureCreateDTO {

    @NotNull(message = "Base ID is required")
    private Long baseId;

    @NotNull(message = "Equipment Type ID is required")
    private Long equipmentTypeId;

    private Long assignmentId;

    @NotNull(message = "Expended quantity is required")
    @Min(value = 1, message = "Expended quantity must be at least 1")
    private Integer expendedQuantity;

    @NotBlank(message = "Reason for expenditure is required")
    private String reason;

    private LocalDateTime expendedDate;

    public ExpenditureCreateDTO() {}

    public ExpenditureCreateDTO(Long baseId, Long equipmentTypeId, Long assignmentId, Integer expendedQuantity, String reason, LocalDateTime expendedDate) {
        this.baseId = baseId;
        this.equipmentTypeId = equipmentTypeId;
        this.assignmentId = assignmentId;
        this.expendedQuantity = expendedQuantity;
        this.reason = reason;
        this.expendedDate = expendedDate;
    }

    public Long getBaseId() { return baseId; }
    public void setBaseId(Long baseId) { this.baseId = baseId; }

    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public void setEquipmentTypeId(Long equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Integer getExpendedQuantity() { return expendedQuantity; }
    public void setExpendedQuantity(Integer expendedQuantity) { this.expendedQuantity = expendedQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getExpendedDate() { return expendedDate; }
    public void setExpendedDate(LocalDateTime expendedDate) { this.expendedDate = expendedDate; }
}
