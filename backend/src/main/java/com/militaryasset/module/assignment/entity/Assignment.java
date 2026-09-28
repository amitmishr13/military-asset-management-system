package com.militaryasset.module.assignment.entity;

import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.equipment.entity.EquipmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments", indexes = {
    @Index(name = "idx_assignments_base_equip_status", columnList = "base_id, equipment_type_id, status")
})
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_reference", nullable = false, unique = true, length = 50)
    private String assignmentReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "personnel_name", nullable = false, length = 100)
    private String personnelName;

    @Column(name = "personnel_rank", length = 50)
    private String personnelRank;

    @Column(name = "personnel_id", nullable = false, length = 50)
    private String personnelId;

    @Column(name = "assigned_quantity", nullable = false)
    private Integer assignedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Assignment() {}

    public Assignment(Long id, String assignmentReference, Base base, EquipmentType equipmentType, String personnelName, String personnelRank, String personnelId, Integer assignedQuantity, AssignmentStatus status, LocalDateTime assignedDate, LocalDateTime returnedDate, User assignedByUser, LocalDateTime createdAt) {
        this.id = id;
        this.assignmentReference = assignmentReference;
        this.base = base;
        this.equipmentType = equipmentType;
        this.personnelName = personnelName;
        this.personnelRank = personnelRank;
        this.personnelId = personnelId;
        this.assignedQuantity = assignedQuantity;
        this.status = status;
        this.assignedDate = assignedDate;
        this.returnedDate = returnedDate;
        this.assignedByUser = assignedByUser;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssignmentReference() { return assignmentReference; }
    public void setAssignmentReference(String assignmentReference) { this.assignmentReference = assignmentReference; }

    public Base getBase() { return base; }
    public void setBase(Base base) { this.base = base; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String personnelName) { this.personnelName = personnelName; }

    public String getPersonnelRank() { return personnelRank; }
    public void setPersonnelRank(String personnelRank) { this.personnelRank = personnelRank; }

    public String getPersonnelId() { return personnelId; }
    public void setPersonnelId(String personnelId) { this.personnelId = personnelId; }

    public Integer getAssignedQuantity() { return assignedQuantity; }
    public void setAssignedQuantity(Integer assignedQuantity) { this.assignedQuantity = assignedQuantity; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public LocalDateTime getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDateTime returnedDate) { this.returnedDate = returnedDate; }

    public User getAssignedByUser() { return assignedByUser; }
    public void setAssignedByUser(User assignedByUser) { this.assignedByUser = assignedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static AssignmentBuilder builder() {
        return new AssignmentBuilder();
    }

    public static class AssignmentBuilder {
        private Long id;
        private String assignmentReference;
        private Base base;
        private EquipmentType equipmentType;
        private String personnelName;
        private String personnelRank;
        private String personnelId;
        private Integer assignedQuantity;
        private AssignmentStatus status;
        private LocalDateTime assignedDate;
        private LocalDateTime returnedDate;
        private User assignedByUser;
        private LocalDateTime createdAt;

        public AssignmentBuilder id(Long id) { this.id = id; return this; }
        public AssignmentBuilder assignmentReference(String assignmentReference) { this.assignmentReference = assignmentReference; return this; }
        public AssignmentBuilder base(Base base) { this.base = base; return this; }
        public AssignmentBuilder equipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; return this; }
        public AssignmentBuilder personnelName(String personnelName) { this.personnelName = personnelName; return this; }
        public AssignmentBuilder personnelRank(String personnelRank) { this.personnelRank = personnelRank; return this; }
        public AssignmentBuilder personnelId(String personnelId) { this.personnelId = personnelId; return this; }
        public AssignmentBuilder assignedQuantity(Integer assignedQuantity) { this.assignedQuantity = assignedQuantity; return this; }
        public AssignmentBuilder status(AssignmentStatus status) { this.status = status; return this; }
        public AssignmentBuilder assignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; return this; }
        public AssignmentBuilder returnedDate(LocalDateTime returnedDate) { this.returnedDate = returnedDate; return this; }
        public AssignmentBuilder assignedByUser(User assignedByUser) { this.assignedByUser = assignedByUser; return this; }
        public AssignmentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Assignment build() {
            return new Assignment(id, assignmentReference, base, equipmentType, personnelName, personnelRank, personnelId, assignedQuantity, status, assignedDate, returnedDate, assignedByUser, createdAt);
        }
    }
}
