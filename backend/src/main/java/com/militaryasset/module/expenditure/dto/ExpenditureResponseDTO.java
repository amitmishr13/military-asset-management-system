package com.militaryasset.module.expenditure.dto;

import com.militaryasset.module.expenditure.entity.Expenditure;
import java.time.LocalDateTime;

public class ExpenditureResponseDTO {

    private Long id;
    private String expenditureReference;
    private Long baseId;
    private String baseCode;
    private String baseName;
    private Long equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private String category;
    private String unitOfMeasure;
    private Long assignmentId;
    private String assignmentReference;
    private Integer expendedQuantity;
    private String reason;
    private LocalDateTime expendedDate;
    private Long recordedByUserId;
    private String recordedByUsername;
    private LocalDateTime createdAt;

    public ExpenditureResponseDTO() {}

    public ExpenditureResponseDTO(Expenditure expenditure) {
        this.id = expenditure.getId();
        this.expenditureReference = expenditure.getExpenditureReference();
        if (expenditure.getBase() != null) {
            this.baseId = expenditure.getBase().getId();
            this.baseCode = expenditure.getBase().getCode();
            this.baseName = expenditure.getBase().getName();
        }
        if (expenditure.getEquipmentType() != null) {
            this.equipmentTypeId = expenditure.getEquipmentType().getId();
            this.equipmentCode = expenditure.getEquipmentType().getCode();
            this.equipmentName = expenditure.getEquipmentType().getName();
            this.category = expenditure.getEquipmentType().getCategory();
            this.unitOfMeasure = expenditure.getEquipmentType().getUnitOfMeasure();
        }
        if (expenditure.getAssignment() != null) {
            this.assignmentId = expenditure.getAssignment().getId();
            this.assignmentReference = expenditure.getAssignment().getAssignmentReference();
        }
        this.expendedQuantity = expenditure.getExpendedQuantity();
        this.reason = expenditure.getReason();
        this.expendedDate = expenditure.getExpendedDate();
        if (expenditure.getRecordedByUser() != null) {
            this.recordedByUserId = expenditure.getRecordedByUser().getId();
            this.recordedByUsername = expenditure.getRecordedByUser().getUsername();
        }
        this.createdAt = expenditure.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getExpenditureReference() { return expenditureReference; }
    public Long getBaseId() { return baseId; }
    public String getBaseCode() { return baseCode; }
    public String getBaseName() { return baseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public Long getAssignmentId() { return assignmentId; }
    public String getAssignmentReference() { return assignmentReference; }
    public Integer getExpendedQuantity() { return expendedQuantity; }
    public String getReason() { return reason; }
    public LocalDateTime getExpendedDate() { return expendedDate; }
    public Long getRecordedByUserId() { return recordedByUserId; }
    public String getRecordedByUsername() { return recordedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
