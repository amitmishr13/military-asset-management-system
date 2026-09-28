package com.militaryasset.module.purchase.service;

import com.militaryasset.common.exception.ResourceNotFoundException;
import com.militaryasset.module.audit.service.AuditLogService;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.equipment.entity.EquipmentType;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import com.militaryasset.module.purchase.dto.PurchaseCreateDTO;
import com.militaryasset.module.purchase.dto.PurchaseResponseDTO;
import com.militaryasset.module.purchase.entity.Purchase;
import com.militaryasset.module.purchase.repository.PurchaseRepository;
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
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaseSecurityService baseSecurityService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public PurchaseResponseDTO createPurchase(PurchaseCreateDTO dto, CustomUserDetails currentUser) {
        if (!baseSecurityService.isAccessAllowedForBase(dto.getBaseId())) {
            throw new AccessDeniedException("Forbidden: You are not authorized to log purchases for Base ID " + dto.getBaseId());
        }

        Base base = baseRepository.findById(dto.getBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Base not found with ID: " + dto.getBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(dto.getEquipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment Type not found with ID: " + dto.getEquipmentTypeId()));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getUsername()));

        String reference = generatePurchaseReference();

        Purchase purchase = Purchase.builder()
                .purchaseReference(reference)
                .base(base)
                .equipmentType(equipmentType)
                .quantity(dto.getQuantity())
                .unitCost(dto.getUnitCost())
                .purchaseDate(dto.getPurchaseDate())
                .supplierDetails(dto.getSupplierDetails())
                .recordedByUser(user)
                .build();

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Record audit trail
        String auditDetails = String.format("Recorded purchase of %d %s (%s) for base %s",
                savedPurchase.getQuantity(), equipmentType.getName(), equipmentType.getCode(), base.getName());
        auditLogService.logAction(user.getId(), user.getUsername(), "RECORD_PURCHASE", "PURCHASE", savedPurchase.getId(), base.getId(), auditDetails, "127.0.0.1");

        return new PurchaseResponseDTO(savedPurchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponseDTO> getPurchases(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate, CustomUserDetails currentUser) {
        Long effectiveBaseId = baseId;

        // Base Commander restriction
        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view purchases for their assigned base");
            }
            effectiveBaseId = currentUser.getBaseId();
        }

        final Long filterBaseId = effectiveBaseId;

        return purchaseRepository.findAll().stream()
                .filter(p -> filterBaseId == null || p.getBase().getId().equals(filterBaseId))
                .filter(p -> equipmentTypeId == null || p.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(p -> startDate == null || !p.getPurchaseDate().toLocalDate().isBefore(startDate))
                .filter(p -> endDate == null || !p.getPurchaseDate().toLocalDate().isAfter(endDate))
                .map(PurchaseResponseDTO::new)
                .collect(Collectors.toList());
    }

    private String generatePurchaseReference() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PUR-" + dateStr + "-" + shortUuid;
    }
}
