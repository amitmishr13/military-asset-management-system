package com.militaryasset.module.transfer.service;

import com.militaryasset.common.exception.BadRequestException;
import com.militaryasset.common.exception.InsufficientStockException;
import com.militaryasset.common.exception.ResourceNotFoundException;
import com.militaryasset.module.audit.service.AuditLogService;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.equipment.entity.EquipmentType;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import com.militaryasset.module.equipment.service.InventoryStockService;
import com.militaryasset.module.transfer.dto.TransferCreateDTO;
import com.militaryasset.module.transfer.dto.TransferResponseDTO;
import com.militaryasset.module.transfer.entity.Transfer;
import com.militaryasset.module.transfer.repository.TransferRepository;
import com.militaryasset.security.BaseSecurityService;
import com.militaryasset.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryStockService inventoryStockService;

    @Autowired
    private BaseSecurityService baseSecurityService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public TransferResponseDTO createTransfer(TransferCreateDTO dto, CustomUserDetails currentUser) {
        if (dto.getSourceBaseId().equals(dto.getDestinationBaseId())) {
            throw new BadRequestException("Source base and destination base cannot be the same base");
        }

        if (!baseSecurityService.isTransferAllowed(dto.getSourceBaseId(), dto.getDestinationBaseId())) {
            throw new AccessDeniedException("Forbidden: Base Commanders can only initiate transfers where their assigned base is the source base");
        }

        Base sourceBase = baseRepository.findById(dto.getSourceBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source base not found with ID: " + dto.getSourceBaseId()));

        Base destinationBase = baseRepository.findById(dto.getDestinationBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination base not found with ID: " + dto.getDestinationBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(dto.getEquipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found with ID: " + dto.getEquipmentTypeId()));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getUsername()));

        // Calculate available stock at source base
        int availableStock = inventoryStockService.getAvailableStock(sourceBase.getId(), equipmentType.getId());
        if (availableStock < dto.getQuantity()) {
            throw new InsufficientStockException(String.format(
                    "Insufficient available stock at source base (%s) for equipment %s. Available: %d, Requested: %d",
                    sourceBase.getName(), equipmentType.getName(), availableStock, dto.getQuantity()));
        }

        String reference = generateTransferReference();

        Transfer transfer = Transfer.builder()
                .transferReference(reference)
                .sourceBase(sourceBase)
                .destinationBase(destinationBase)
                .equipmentType(equipmentType)
                .quantity(dto.getQuantity())
                .transferDate(dto.getTransferDate())
                .remarks(dto.getRemarks())
                .initiatedByUser(user)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);

        // Record audit log
        String auditDetails = String.format("Transferred %d %s (%s) from %s to %s",
                savedTransfer.getQuantity(), equipmentType.getName(), equipmentType.getCode(), sourceBase.getName(), destinationBase.getName());
        auditLogService.logAction(user.getId(), user.getUsername(), "INITIATE_TRANSFER", "TRANSFER", savedTransfer.getId(), sourceBase.getId(), auditDetails, "127.0.0.1");

        return new TransferResponseDTO(savedTransfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getTransfers(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate, CustomUserDetails currentUser) {
        Long filterBaseId = baseId;

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view transfer records involving their assigned base");
            }
            filterBaseId = currentUser.getBaseId();
        }

        final Long targetBaseId = filterBaseId;

        return transferRepository.findAll().stream()
                .filter(t -> targetBaseId == null || t.getSourceBase().getId().equals(targetBaseId) || t.getDestinationBase().getId().equals(targetBaseId))
                .filter(t -> equipmentTypeId == null || t.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(t -> startDate == null || !t.getTransferDate().toLocalDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransferDate().toLocalDate().isAfter(endDate))
                .map(TransferResponseDTO::new)
                .collect(Collectors.toList());
    }

    private String generateTransferReference() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TRN-" + dateStr + "-" + shortUuid;
    }
}
