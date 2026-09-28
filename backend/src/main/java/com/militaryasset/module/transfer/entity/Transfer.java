package com.militaryasset.module.transfer.entity;

import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.equipment.entity.EquipmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers", indexes = {
    @Index(name = "idx_transfers_source", columnList = "source_base_id, equipment_type_id, transfer_date"),
    @Index(name = "idx_transfers_dest", columnList = "destination_base_id, equipment_type_id, transfer_date")
})
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_reference", nullable = false, unique = true, length = 50)
    private String transferReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_base_id", nullable = false)
    private Base sourceBase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_base_id", nullable = false)
    private Base destinationBase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by_user_id", nullable = false)
    private User initiatedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transfer() {}

    public Transfer(Long id, String transferReference, Base sourceBase, Base destinationBase, EquipmentType equipmentType, Integer quantity, LocalDateTime transferDate, String remarks, User initiatedByUser, LocalDateTime createdAt) {
        this.id = id;
        this.transferReference = transferReference;
        this.sourceBase = sourceBase;
        this.destinationBase = destinationBase;
        this.equipmentType = equipmentType;
        this.quantity = quantity;
        this.transferDate = transferDate;
        this.remarks = remarks;
        this.initiatedByUser = initiatedByUser;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransferReference() { return transferReference; }
    public void setTransferReference(String transferReference) { this.transferReference = transferReference; }

    public Base getSourceBase() { return sourceBase; }
    public void setSourceBase(Base sourceBase) { this.sourceBase = sourceBase; }

    public Base getDestinationBase() { return destinationBase; }
    public void setDestinationBase(Base destinationBase) { this.destinationBase = destinationBase; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public User getInitiatedByUser() { return initiatedByUser; }
    public void setInitiatedByUser(User initiatedByUser) { this.initiatedByUser = initiatedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TransferBuilder builder() {
        return new TransferBuilder();
    }

    public static class TransferBuilder {
        private Long id;
        private String transferReference;
        private Base sourceBase;
        private Base destinationBase;
        private EquipmentType equipmentType;
        private Integer quantity;
        private LocalDateTime transferDate;
        private String remarks;
        private User initiatedByUser;
        private LocalDateTime createdAt;

        public TransferBuilder id(Long id) { this.id = id; return this; }
        public TransferBuilder transferReference(String transferReference) { this.transferReference = transferReference; return this; }
        public TransferBuilder sourceBase(Base sourceBase) { this.sourceBase = sourceBase; return this; }
        public TransferBuilder destinationBase(Base destinationBase) { this.destinationBase = destinationBase; return this; }
        public TransferBuilder equipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; return this; }
        public TransferBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public TransferBuilder transferDate(LocalDateTime transferDate) { this.transferDate = transferDate; return this; }
        public TransferBuilder remarks(String remarks) { this.remarks = remarks; return this; }
        public TransferBuilder initiatedByUser(User initiatedByUser) { this.initiatedByUser = initiatedByUser; return this; }
        public TransferBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transfer build() {
            return new Transfer(id, transferReference, sourceBase, destinationBase, equipmentType, quantity, transferDate, remarks, initiatedByUser, createdAt);
        }
    }
}
