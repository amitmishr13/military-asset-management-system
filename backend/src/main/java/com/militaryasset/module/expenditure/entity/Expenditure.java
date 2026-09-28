package com.militaryasset.module.expenditure.entity;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.equipment.entity.EquipmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenditures", indexes = {
    @Index(name = "idx_expenditures_base_equip_date", columnList = "base_id, equipment_type_id, expended_date")
})
public class Expenditure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expenditure_reference", nullable = false, unique = true, length = 50)
    private String expenditureReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(name = "expended_quantity", nullable = false)
    private Integer expendedQuantity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "expended_date", nullable = false)
    private LocalDateTime expendedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Expenditure() {}

    public Expenditure(Long id, String expenditureReference, Base base, EquipmentType equipmentType, Assignment assignment, Integer expendedQuantity, String reason, LocalDateTime expendedDate, User recordedByUser, LocalDateTime createdAt) {
        this.id = id;
        this.expenditureReference = expenditureReference;
        this.base = base;
        this.equipmentType = equipmentType;
        this.assignment = assignment;
        this.expendedQuantity = expendedQuantity;
        this.reason = reason;
        this.expendedDate = expendedDate;
        this.recordedByUser = recordedByUser;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExpenditureReference() { return expenditureReference; }
    public void setExpenditureReference(String expenditureReference) { this.expenditureReference = expenditureReference; }

    public Base getBase() { return base; }
    public void setBase(Base base) { this.base = base; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public Integer getExpendedQuantity() { return expendedQuantity; }
    public void setExpendedQuantity(Integer expendedQuantity) { this.expendedQuantity = expendedQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getExpendedDate() { return expendedDate; }
    public void setExpendedDate(LocalDateTime expendedDate) { this.expendedDate = expendedDate; }

    public User getRecordedByUser() { return recordedByUser; }
    public void setRecordedByUser(User recordedByUser) { this.recordedByUser = recordedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ExpenditureBuilder builder() {
        return new ExpenditureBuilder();
    }

    public static class ExpenditureBuilder {
        private Long id;
        private String expenditureReference;
        private Base base;
        private EquipmentType equipmentType;
        private Assignment assignment;
        private Integer expendedQuantity;
        private String reason;
        private LocalDateTime expendedDate;
        private User recordedByUser;
        private LocalDateTime createdAt;

        public ExpenditureBuilder id(Long id) { this.id = id; return this; }
        public ExpenditureBuilder expenditureReference(String expenditureReference) { this.expenditureReference = expenditureReference; return this; }
        public ExpenditureBuilder base(Base base) { this.base = base; return this; }
        public ExpenditureBuilder equipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; return this; }
        public ExpenditureBuilder assignment(Assignment assignment) { this.assignment = assignment; return this; }
        public ExpenditureBuilder expendedQuantity(Integer expendedQuantity) { this.expendedQuantity = expendedQuantity; return this; }
        public ExpenditureBuilder reason(String reason) { this.reason = reason; return this; }
        public ExpenditureBuilder expendedDate(LocalDateTime expendedDate) { this.expendedDate = expendedDate; return this; }
        public ExpenditureBuilder recordedByUser(User recordedByUser) { this.recordedByUser = recordedByUser; return this; }
        public ExpenditureBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Expenditure build() {
            return new Expenditure(id, expenditureReference, base, equipmentType, assignment, expendedQuantity, reason, expendedDate, recordedByUser, createdAt);
        }
    }
}
