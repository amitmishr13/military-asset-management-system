package com.militaryasset.module.assignment.dto;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import java.time.LocalDateTime;

public class AssignmentResponseDTO {

    private Long id;
    private String assignmentReference;
    private Long baseId;
    private String baseCode;
    private String baseName;
    private Long equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private String category;
    private String unitOfMeasure;
    private String personnelName;
    private String personnelRank;
    private String personnelId;
    private Integer assignedQuantity;
    private AssignmentStatus status;
    private LocalDateTime assignedDate;
    private LocalDateTime returnedDate;
    private Long assignedByUserId;
    private String assignedByUsername;
    private LocalDateTime createdAt;

    public AssignmentResponseDTO() {}

    public AssignmentResponseDTO(Assignment assignment) {
        this.id = assignment.getId();
        this.assignmentReference = assignment.getAssignmentReference();
        if (assignment.getBase() != null) {
            this.baseId = assignment.getBase().getId();
            this.baseCode = assignment.getBase().getCode();
            this.baseName = assignment.getBase().getName();
        }
        if (assignment.getEquipmentType() != null) {
            this.equipmentTypeId = assignment.getEquipmentType().getId();
            this.equipmentCode = assignment.getEquipmentType().getCode();
            this.equipmentName = assignment.getEquipmentType().getName();
            this.category = assignment.getEquipmentType().getCategory();
            this.unitOfMeasure = assignment.getEquipmentType().getUnitOfMeasure();
        }
        this.personnelName = assignment.getPersonnelName();
        this.personnelRank = assignment.getPersonnelRank();
        this.personnelId = assignment.getPersonnelId();
        this.assignedQuantity = assignment.getAssignedQuantity();
        this.status = assignment.getStatus();
        this.assignedDate = assignment.getAssignedDate();
        this.returnedDate = assignment.getReturnedDate();
        if (assignment.getAssignedByUser() != null) {
            this.assignedByUserId = assignment.getAssignedByUser().getId();
            this.assignedByUsername = assignment.getAssignedByUser().getUsername();
        }
        this.createdAt = assignment.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getAssignmentReference() { return assignmentReference; }
    public Long getBaseId() { return baseId; }
    public String getBaseCode() { return baseCode; }
    public String getBaseName() { return baseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public String getPersonnelName() { return personnelName; }
    public String getPersonnelRank() { return personnelRank; }
    public String getPersonnelId() { return personnelId; }
    public Integer getAssignedQuantity() { return assignedQuantity; }
    public AssignmentStatus getStatus() { return status; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
    public LocalDateTime getReturnedDate() { return returnedDate; }
    public Long getAssignedByUserId() { return assignedByUserId; }
    public String getAssignedByUsername() { return assignedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
