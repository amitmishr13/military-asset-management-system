package com.militaryasset.module.dashboard.service;

import com.militaryasset.common.exception.ResourceNotFoundException;
import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.repository.AssignmentRepository;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.dashboard.dto.DashboardMetricsDTO;
import com.militaryasset.module.dashboard.dto.NetMovementBreakdownDTO;
import com.militaryasset.module.dashboard.dto.NetMovementDetailItemDTO;
import com.militaryasset.module.equipment.entity.EquipmentType;
import com.militaryasset.module.equipment.entity.InitialInventory;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import com.militaryasset.module.equipment.repository.InitialInventoryRepository;
import com.militaryasset.module.expenditure.entity.Expenditure;
import com.militaryasset.module.expenditure.repository.ExpenditureRepository;
import com.militaryasset.module.purchase.entity.Purchase;
import com.militaryasset.module.purchase.repository.PurchaseRepository;
import com.militaryasset.module.transfer.entity.Transfer;
import com.militaryasset.module.transfer.repository.TransferRepository;
import com.militaryasset.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

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

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsDTO getDashboardMetrics(LocalDate date, Long baseId, Long equipmentTypeId, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Dashboard access is not granted to Logistics Officers");
        }

        Long filterBaseId = baseId;

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view dashboard metrics for their assigned base");
            }
            filterBaseId = currentUser.getBaseId();
        }

        final LocalDate targetDate = (date != null) ? date : LocalDate.now();
        final Long targetBaseId = filterBaseId;

        String baseName = null;
        if (targetBaseId != null) {
            Base base = baseRepository.findById(targetBaseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Base not found with ID: " + targetBaseId));
            baseName = base.getName();
        }

        String equipmentName = null;
        if (equipmentTypeId != null) {
            EquipmentType equipmentType = equipmentTypeRepository.findById(equipmentTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment Type not found with ID: " + equipmentTypeId));
            equipmentName = equipmentType.getName();
        }

        // 1. Initial Inventory Baseline
        int initialStock = initialInventoryRepository.findAll().stream()
                .filter(inv -> targetBaseId == null || inv.getBase().getId().equals(targetBaseId))
                .filter(inv -> equipmentTypeId == null || inv.getEquipmentType().getId().equals(equipmentTypeId))
                .mapToInt(InitialInventory::getInitialQuantity)
                .sum();

        // 2. Prior Transactions (Strictly Before targetDate)
        int priorPurchases = purchaseRepository.findAll().stream()
                .filter(p -> targetBaseId == null || p.getBase().getId().equals(targetBaseId))
                .filter(p -> equipmentTypeId == null || p.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(p -> p.getPurchaseDate().toLocalDate().isBefore(targetDate))
                .mapToInt(Purchase::getQuantity)
                .sum();

        List<Transfer> allTransfers = transferRepository.findAll();

        int priorTransferIn = allTransfers.stream()
                .filter(t -> targetBaseId == null || t.getDestinationBase().getId().equals(targetBaseId))
                .filter(t -> equipmentTypeId == null || t.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(t -> t.getTransferDate().toLocalDate().isBefore(targetDate))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int priorTransferOut = allTransfers.stream()
                .filter(t -> targetBaseId == null || t.getSourceBase().getId().equals(targetBaseId))
                .filter(t -> equipmentTypeId == null || t.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(t -> t.getTransferDate().toLocalDate().isBefore(targetDate))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int priorExpenditures = expenditureRepository.findAll().stream()
                .filter(e -> targetBaseId == null || e.getBase().getId().equals(targetBaseId))
                .filter(e -> equipmentTypeId == null || e.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(e -> e.getExpendedDate().toLocalDate().isBefore(targetDate))
                .mapToInt(Expenditure::getExpendedQuantity)
                .sum();

        int openingBalance = initialStock + priorPurchases + priorTransferIn - priorTransferOut - priorExpenditures;

        // 3. Selected-Date Movement (Transactions ON targetDate)
        int dayPurchases = purchaseRepository.findAll().stream()
                .filter(p -> targetBaseId == null || p.getBase().getId().equals(targetBaseId))
                .filter(p -> equipmentTypeId == null || p.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(p -> p.getPurchaseDate().toLocalDate().isEqual(targetDate))
                .mapToInt(Purchase::getQuantity)
                .sum();

        int dayTransferIn = allTransfers.stream()
                .filter(t -> targetBaseId == null || t.getDestinationBase().getId().equals(targetBaseId))
                .filter(t -> equipmentTypeId == null || t.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(t -> t.getTransferDate().toLocalDate().isEqual(targetDate))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int dayTransferOut = allTransfers.stream()
                .filter(t -> targetBaseId == null || t.getSourceBase().getId().equals(targetBaseId))
                .filter(t -> equipmentTypeId == null || t.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(t -> t.getTransferDate().toLocalDate().isEqual(targetDate))
                .mapToInt(Transfer::getQuantity)
                .sum();

        int dayExpenditures = expenditureRepository.findAll().stream()
                .filter(e -> targetBaseId == null || e.getBase().getId().equals(targetBaseId))
                .filter(e -> equipmentTypeId == null || e.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(e -> e.getExpendedDate().toLocalDate().isEqual(targetDate))
                .mapToInt(Expenditure::getExpendedQuantity)
                .sum();

        int netMovement = dayPurchases + dayTransferIn - dayTransferOut;
        int closingBalance = openingBalance + netMovement - dayExpenditures;

        // 4. Active Assigned Assets As Of targetDate
        List<Expenditure> allExpenditures = expenditureRepository.findAll();

        int activeAssignedAssets = assignmentRepository.findAll().stream()
                .filter(a -> targetBaseId == null || a.getBase().getId().equals(targetBaseId))
                .filter(a -> equipmentTypeId == null || a.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(a -> !a.getAssignedDate().toLocalDate().isAfter(targetDate))
                .filter(a -> a.getReturnedDate() == null || a.getReturnedDate().toLocalDate().isAfter(targetDate))
                .mapToInt(a -> {
                    int linkedExpended = allExpenditures.stream()
                            .filter(e -> e.getAssignment() != null && e.getAssignment().getId().equals(a.getId()))
                            .filter(e -> !e.getExpendedDate().toLocalDate().isAfter(targetDate))
                            .mapToInt(Expenditure::getExpendedQuantity)
                            .sum();
                    return Math.max(0, a.getAssignedQuantity() - linkedExpended);
                })
                .sum();

        int availableStock = Math.max(0, closingBalance - activeAssignedAssets);

        return new DashboardMetricsDTO(
                targetDate, targetBaseId, baseName, equipmentTypeId, equipmentName,
                openingBalance, dayPurchases, dayTransferIn, dayTransferOut, netMovement,
                dayExpenditures, closingBalance, activeAssignedAssets, availableStock
        );
    }

    @Transactional(readOnly = true)
    public NetMovementBreakdownDTO getNetMovementDetails(LocalDate date, Long baseId, Long equipmentTypeId, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Dashboard access is not granted to Logistics Officers");
        }

        Long filterBaseId = baseId;

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view dashboard metrics for their assigned base");
            }
            filterBaseId = currentUser.getBaseId();
        }

        final LocalDate targetDate = (date != null) ? date : LocalDate.now();
        final Long targetBaseId = filterBaseId;

        String baseName = null;
        if (targetBaseId != null) {
            Base base = baseRepository.findById(targetBaseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Base not found with ID: " + targetBaseId));
            baseName = base.getName();
        }

        String equipmentName = null;
        if (equipmentTypeId != null) {
            EquipmentType equipmentType = equipmentTypeRepository.findById(equipmentTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment Type not found with ID: " + equipmentTypeId));
            equipmentName = equipmentType.getName();
        }

        List<NetMovementDetailItemDTO> items = new ArrayList<>();

        // 1. Day Purchases
        List<Purchase> purchases = purchaseRepository.findAll().stream()
                .filter(p -> targetBaseId == null || p.getBase().getId().equals(targetBaseId))
                .filter(p -> equipmentTypeId == null || p.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(p -> p.getPurchaseDate().toLocalDate().isEqual(targetDate))
                .toList();

        for (Purchase p : purchases) {
            items.add(new NetMovementDetailItemDTO(
                    "PURCHASE",
                    p.getPurchaseReference(),
                    p.getPurchaseDate(),
                    p.getBase().getId(),
                    p.getBase().getName(),
                    null,
                    null,
                    p.getEquipmentType().getId(),
                    p.getEquipmentType().getCode(),
                    p.getEquipmentType().getName(),
                    p.getQuantity(),
                    +p.getQuantity(),
                    p.getRecordedByUser() != null ? p.getRecordedByUser().getUsername() : "N/A",
                    p.getSupplierDetails() != null ? "Supplier: " + p.getSupplierDetails() : "Procurement"
            ));
        }

        // 2. Day Transfers
        List<Transfer> transfers = transferRepository.findAll();

        for (Transfer t : transfers) {
            if (!t.getTransferDate().toLocalDate().isEqual(targetDate)) {
                continue;
            }
            if (equipmentTypeId != null && !t.getEquipmentType().getId().equals(equipmentTypeId)) {
                continue;
            }

            // Transfer In
            if (targetBaseId == null || t.getDestinationBase().getId().equals(targetBaseId)) {
                // If targetBaseId is null (global view), only count once per transaction or include clear direction
                if (targetBaseId != null || !t.getSourceBase().getId().equals(t.getDestinationBase().getId())) {
                    if (targetBaseId == null || t.getDestinationBase().getId().equals(targetBaseId)) {
                        items.add(new NetMovementDetailItemDTO(
                                "TRANSFER_IN",
                                t.getTransferReference(),
                                t.getTransferDate(),
                                t.getDestinationBase().getId(),
                                t.getDestinationBase().getName(),
                                t.getSourceBase().getId(),
                                t.getSourceBase().getName(),
                                t.getEquipmentType().getId(),
                                t.getEquipmentType().getCode(),
                                t.getEquipmentType().getName(),
                                t.getQuantity(),
                                +t.getQuantity(),
                                t.getInitiatedByUser() != null ? t.getInitiatedByUser().getUsername() : "N/A",
                                t.getRemarks() != null ? t.getRemarks() : "Inter-base transfer in"
                        ));
                    }
                }
            }

            // Transfer Out
            if (targetBaseId == null || t.getSourceBase().getId().equals(targetBaseId)) {
                items.add(new NetMovementDetailItemDTO(
                        "TRANSFER_OUT",
                        t.getTransferReference(),
                        t.getTransferDate(),
                        t.getSourceBase().getId(),
                        t.getSourceBase().getName(),
                        t.getDestinationBase().getId(),
                        t.getDestinationBase().getName(),
                        t.getEquipmentType().getId(),
                        t.getEquipmentType().getCode(),
                        t.getEquipmentType().getName(),
                        t.getQuantity(),
                        -t.getQuantity(),
                        t.getInitiatedByUser() != null ? t.getInitiatedByUser().getUsername() : "N/A",
                        t.getRemarks() != null ? t.getRemarks() : "Inter-base transfer out"
                ));
            }
        }

        int totalPurchases = items.stream().filter(i -> "PURCHASE".equals(i.getTransactionType())).mapToInt(NetMovementDetailItemDTO::getQuantity).sum();
        int totalTransferIn = items.stream().filter(i -> "TRANSFER_IN".equals(i.getTransactionType())).mapToInt(NetMovementDetailItemDTO::getQuantity).sum();
        int totalTransferOut = items.stream().filter(i -> "TRANSFER_OUT".equals(i.getTransactionType())).mapToInt(NetMovementDetailItemDTO::getQuantity).sum();
        int netMovement = totalPurchases + totalTransferIn - totalTransferOut;

        return new NetMovementBreakdownDTO(
                targetDate, targetBaseId, baseName, equipmentTypeId, equipmentName,
                totalPurchases, totalTransferIn, totalTransferOut, netMovement, items
        );
    }
}
