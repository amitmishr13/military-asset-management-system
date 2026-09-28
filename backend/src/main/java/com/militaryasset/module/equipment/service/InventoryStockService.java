package com.militaryasset.module.equipment.service;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.repository.AssignmentRepository;
import com.militaryasset.module.equipment.entity.InitialInventory;
import com.militaryasset.module.equipment.repository.InitialInventoryRepository;
import com.militaryasset.module.expenditure.entity.Expenditure;
import com.militaryasset.module.expenditure.repository.ExpenditureRepository;
import com.militaryasset.module.purchase.entity.Purchase;
import com.militaryasset.module.purchase.repository.PurchaseRepository;
import com.militaryasset.module.transfer.entity.Transfer;
import com.militaryasset.module.transfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryStockService {

    @Autowired
    private InitialInventoryRepository initialInventoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ExpenditureRepository expenditureRepository;

    @Transactional(readOnly = true)
    public int getAvailableStock(Long baseId, Long equipmentTypeId) {
        int closingBalance = getClosingBalance(baseId, equipmentTypeId);
        int activeAssignments = getActiveAssignedAssets(baseId, equipmentTypeId);
        return Math.max(0, closingBalance - activeAssignments);
    }

    @Transactional(readOnly = true)
    public int getClosingBalance(Long baseId, Long equipmentTypeId) {
        int initialStock = initialInventoryRepository.findByBaseIdAndEquipmentTypeId(baseId, equipmentTypeId)
                .map(InitialInventory::getInitialQuantity)
                .orElse(0);

        int totalPurchases = purchaseRepository.findByBaseId(baseId).stream()
                .filter(p -> p.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(Purchase::getQuantity)
                .sum();

        List<Transfer> allTransfers = transferRepository.findBySourceBaseIdOrDestinationBaseId(baseId, baseId);

        int totalTransferIn = allTransfers.stream()
                .filter(t -> t.getDestinationBase().getId().equals(baseId) && t.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int totalTransferOut = allTransfers.stream()
                .filter(t -> t.getSourceBase().getId().equals(baseId) && t.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int totalExpended = expenditureRepository.findByBaseId(baseId).stream()
                .filter(e -> e.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(Expenditure::getExpendedQuantity)
                .sum();

        return initialStock + totalPurchases + totalTransferIn - totalTransferOut - totalExpended;
    }

    @Transactional(readOnly = true)
    public int getActiveAssignedAssets(Long baseId, Long equipmentTypeId) {
        return assignmentRepository.findByBaseIdAndStatus(baseId, AssignmentStatus.ACTIVE).stream()
                .filter(a -> a.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(a -> Math.max(0, a.getAssignedQuantity() - getLinkedExpendedQuantity(a.getId())))
                .sum();
    }

    @Transactional(readOnly = true)
    public int getLinkedExpendedQuantity(Long assignmentId) {
        if (assignmentId == null) {
            return 0;
        }
        return expenditureRepository.findByAssignmentId(assignmentId).stream()
                .mapToInt(Expenditure::getExpendedQuantity)
                .sum();
    }

    @Transactional(readOnly = true)
    public int getEffectiveAssignedQuantity(Long assignmentId) {
        if (assignmentId == null) {
            return 0;
        }
        return assignmentRepository.findById(assignmentId)
                .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
                .map(a -> Math.max(0, a.getAssignedQuantity() - getLinkedExpendedQuantity(assignmentId)))
                .orElse(0);
    }
}
