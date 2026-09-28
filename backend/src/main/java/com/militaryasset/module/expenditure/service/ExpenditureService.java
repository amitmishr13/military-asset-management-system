package com.militaryasset.module.expenditure.service;

import com.militaryasset.common.exception.BadRequestException;
import com.militaryasset.common.exception.InsufficientStockException;
import com.militaryasset.common.exception.ResourceNotFoundException;
import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.repository.AssignmentRepository;
import com.militaryasset.module.audit.service.AuditLogService;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.equipment.entity.EquipmentType;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import com.militaryasset.module.equipment.service.InventoryStockService;
import com.militaryasset.module.expenditure.dto.ExpenditureCreateDTO;
import com.militaryasset.module.expenditure.dto.ExpenditureResponseDTO;
import com.militaryasset.module.expenditure.entity.Expenditure;
import com.militaryasset.module.expenditure.repository.ExpenditureRepository;
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
public class ExpenditureService {

    @Autowired
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

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
    public ExpenditureResponseDTO createExpenditure(ExpenditureCreateDTO dto, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Logistics Officers do not have permission to manage expenditures");
        }

        if (!baseSecurityService.isAccessAllowedForBase(dto.getBaseId())) {
            throw new AccessDeniedException("Forbidden: Base Commanders can only record expenditures for their assigned base");
        }

        if (dto.getExpendedQuantity() <= 0) {
            throw new BadRequestException("Expended quantity must be greater than 0");
        }

        Base base = baseRepository.findById(dto.getBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Base not found with ID: " + dto.getBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(dto.getEquipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment Type not found with ID: " + dto.getEquipmentTypeId()));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getUsername()));

        Assignment assignment = null;

        if (dto.getAssignmentId() != null) {
            assignment = assignmentRepository.findById(dto.getAssignmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + dto.getAssignmentId()));

            if (!assignment.getBase().getId().equals(base.getId())) {
                throw new BadRequestException("Assignment does not belong to specified base");
            }

            if (!assignment.getEquipmentType().getId().equals(equipmentType.getId())) {
                throw new BadRequestException("Assignment does not match specified equipment type");
            }

            if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
                throw new BadRequestException("Cannot record expenditure against inactive or already expended assignment");
            }

            int effectiveAssignedQuantity = inventoryStockService.getEffectiveAssignedQuantity(assignment.getId());
            if (effectiveAssignedQuantity < dto.getExpendedQuantity()) {
                throw new InsufficientStockException(String.format(
                        "Expenditure quantity (%d) exceeds remaining active assignment quantity (%d) for assignment %s",
                        dto.getExpendedQuantity(), effectiveAssignedQuantity, assignment.getAssignmentReference()));
            }

            if (effectiveAssignedQuantity == dto.getExpendedQuantity()) {
                assignment.setStatus(AssignmentStatus.EXPENDED);
                assignmentRepository.save(assignment);
            }
        } else {
            int availableStock = inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId());
            if (availableStock < dto.getExpendedQuantity()) {
                throw new InsufficientStockException(String.format(
                        "Insufficient available unassigned stock at base (%s) for equipment %s. Available: %d, Requested expenditure: %d",
                        base.getName(), equipmentType.getName(), availableStock, dto.getExpendedQuantity()));
            }
        }

        String reference = generateExpenditureReference();
        LocalDateTime expendedDate = dto.getExpendedDate() != null ? dto.getExpendedDate() : LocalDateTime.now();

        Expenditure expenditure = Expenditure.builder()
                .expenditureReference(reference)
                .base(base)
                .equipmentType(equipmentType)
                .assignment(assignment)
                .expendedQuantity(dto.getExpendedQuantity())
                .reason(dto.getReason())
                .expendedDate(expendedDate)
                .recordedByUser(user)
                .build();

        Expenditure savedExpenditure = expenditureRepository.save(expenditure);

        String auditDetails = String.format("Recorded expenditure of %d %s (%s) at base %s (Reason: %s, Linked Assignment: %s)",
                savedExpenditure.getExpendedQuantity(), equipmentType.getName(), equipmentType.getCode(),
                base.getName(), dto.getReason(), assignment != null ? assignment.getAssignmentReference() : "None");
        auditLogService.logAction(user.getId(), user.getUsername(), "RECORD_EXPENDITURE", "EXPENDITURE", savedExpenditure.getId(), base.getId(), auditDetails, "127.0.0.1");

        return new ExpenditureResponseDTO(savedExpenditure);
    }

    @Transactional(readOnly = true)
    public List<ExpenditureResponseDTO> getExpenditures(Long baseId, Long equipmentTypeId, Long assignmentId, LocalDate startDate, LocalDate endDate, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Logistics Officers do not have permission to view expenditures");
        }

        Long filterBaseId = baseId;

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view expenditures for their assigned base");
            }
            filterBaseId = currentUser.getBaseId();
        }

        final Long targetBaseId = filterBaseId;

        return expenditureRepository.findAll().stream()
                .filter(e -> targetBaseId == null || e.getBase().getId().equals(targetBaseId))
                .filter(e -> equipmentTypeId == null || e.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(e -> assignmentId == null || (e.getAssignment() != null && e.getAssignment().getId().equals(assignmentId)))
                .filter(e -> startDate == null || !e.getExpendedDate().toLocalDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getExpendedDate().toLocalDate().isAfter(endDate))
                .map(ExpenditureResponseDTO::new)
                .collect(Collectors.toList());
    }

    private String generateExpenditureReference() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "EXP-" + dateStr + "-" + shortUuid;
    }
}
