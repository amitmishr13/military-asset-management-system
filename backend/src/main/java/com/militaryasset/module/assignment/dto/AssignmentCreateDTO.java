package com.militaryasset.module.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AssignmentCreateDTO {

    @NotNull(message = "Base ID is required")
    private Long baseId;

    @NotNull(message = "Equipment Type ID is required")
    private Long equipmentTypeId;

    @NotBlank(message = "Personnel name is required")
    private String personnelName;

    private String personnelRank;

    @NotBlank(message = "Personnel ID is required")
    private String personnelId;

    @NotNull(message = "Assigned quantity is required")
    @Min(value = 1, message = "Assigned quantity must be at least 1")
    private Integer assignedQuantity;

    private LocalDateTime assignedDate;

    public AssignmentCreateDTO() {}

    public AssignmentCreateDTO(Long baseId, Long equipmentTypeId, String personnelName, String personnelRank, String personnelId, Integer assignedQuantity, LocalDateTime assignedDate) {
        this.baseId = baseId;
        this.equipmentTypeId = equipmentTypeId;
        this.personnelName = personnelName;
        this.personnelRank = personnelRank;
        this.personnelId = personnelId;
        this.assignedQuantity = assignedQuantity;
        this.assignedDate = assignedDate;
    }

    public Long getBaseId() { return baseId; }
    public void setBaseId(Long baseId) { this.baseId = baseId; }

    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public void setEquipmentTypeId(Long equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String personnelName) { this.personnelName = personnelName; }

    public String getPersonnelRank() { return personnelRank; }
    public void setPersonnelRank(String personnelRank) { this.personnelRank = personnelRank; }

    public String getPersonnelId() { return personnelId; }
    public void setPersonnelId(String personnelId) { this.personnelId = personnelId; }

    public Integer getAssignedQuantity() { return assignedQuantity; }
    public void setAssignedQuantity(Integer assignedQuantity) { this.assignedQuantity = assignedQuantity; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }
}
