package com.militaryasset.module.assignment.service;

import com.militaryasset.common.exception.BadRequestException;
import com.militaryasset.common.exception.InsufficientStockException;
import com.militaryasset.common.exception.ResourceNotFoundException;
import com.militaryasset.module.assignment.dto.AssignmentCreateDTO;
import com.militaryasset.module.assignment.dto.AssignmentResponseDTO;
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
public class AssignmentService {

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
    public AssignmentResponseDTO createAssignment(AssignmentCreateDTO dto, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Logistics Officers do not have permission to manage assignments");
        }

        if (!baseSecurityService.isAccessAllowedForBase(dto.getBaseId())) {
            throw new AccessDeniedException("Forbidden: Base Commanders can only create assignments for their assigned base");
        }

        if (dto.getAssignedQuantity() <= 0) {
            throw new BadRequestException("Assigned quantity must be greater than 0");
        }

        Base base = baseRepository.findById(dto.getBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Base not found with ID: " + dto.getBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(dto.getEquipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment Type not found with ID: " + dto.getEquipmentTypeId()));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getUsername()));

        int availableStock = inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId());
        if (availableStock < dto.getAssignedQuantity()) {
            throw new InsufficientStockException(String.format(
                    "Insufficient available stock at base (%s) for equipment %s. Available: %d, Requested: %d",
                    base.getName(), equipmentType.getName(), availableStock, dto.getAssignedQuantity()));
        }

        String reference = generateAssignmentReference();

        LocalDateTime assignedDate = dto.getAssignedDate() != null ? dto.getAssignedDate() : LocalDateTime.now();

        Assignment assignment = Assignment.builder()
                .assignmentReference(reference)
                .base(base)
                .equipmentType(equipmentType)
                .personnelName(dto.getPersonnelName())
                .personnelRank(dto.getPersonnelRank())
                .personnelId(dto.getPersonnelId())
                .assignedQuantity(dto.getAssignedQuantity())
                .status(AssignmentStatus.ACTIVE)
                .assignedDate(assignedDate)
                .assignedByUser(user)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);

        String auditDetails = String.format("Assigned %d %s (%s) to %s (ID: %s, Rank: %s) at base %s",
                savedAssignment.getAssignedQuantity(), equipmentType.getName(), equipmentType.getCode(),
                savedAssignment.getPersonnelName(), savedAssignment.getPersonnelId(),
                savedAssignment.getPersonnelRank() != null ? savedAssignment.getPersonnelRank() : "N/A", base.getName());
        auditLogService.logAction(user.getId(), user.getUsername(), "CREATE_ASSIGNMENT", "ASSIGNMENT", savedAssignment.getId(), base.getId(), auditDetails, "127.0.0.1");

        return new AssignmentResponseDTO(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getAssignments(Long baseId, Long equipmentTypeId, AssignmentStatus status, LocalDate startDate, LocalDate endDate, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Logistics Officers do not have permission to view assignments");
        }

        Long filterBaseId = baseId;

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            if (baseId != null && !baseId.equals(currentUser.getBaseId())) {
                throw new AccessDeniedException("Forbidden: Base Commanders can only view assignments for their assigned base");
            }
            filterBaseId = currentUser.getBaseId();
        }

        final Long targetBaseId = filterBaseId;

        return assignmentRepository.findAll().stream()
                .filter(a -> targetBaseId == null || a.getBase().getId().equals(targetBaseId))
                .filter(a -> equipmentTypeId == null || a.getEquipmentType().getId().equals(equipmentTypeId))
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> startDate == null || !a.getAssignedDate().toLocalDate().isBefore(startDate))
                .filter(a -> endDate == null || !a.getAssignedDate().toLocalDate().isAfter(endDate))
                .map(AssignmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    private String generateAssignmentReference() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ASN-" + dateStr + "-" + shortUuid;
    }
}
