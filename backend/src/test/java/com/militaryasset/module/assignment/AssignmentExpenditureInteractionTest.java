package com.militaryasset.module.assignment;

import com.militaryasset.common.exception.InsufficientStockException;
import com.militaryasset.module.assignment.dto.AssignmentCreateDTO;
import com.militaryasset.module.assignment.dto.AssignmentResponseDTO;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.service.AssignmentService;
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
import com.militaryasset.module.expenditure.service.ExpenditureService;
import com.militaryasset.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AssignmentExpenditureInteractionTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ExpenditureService expenditureService;

    @Autowired
    private InventoryStockService inventoryStockService;

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Verify Assignment & Linked Expenditure Stock Accounting Without Double Counting")
    public void testAssignmentAndExpenditureStockInteraction() {
        Base base = baseRepository.findAll().get(0);
        EquipmentType equipmentType = equipmentTypeRepository.findAll().get(0);
        User adminUser = userRepository.findByUsername("admin").orElseThrow();

        CustomUserDetails adminPrincipal = new CustomUserDetails(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        int initialClosingBalance = inventoryStockService.getClosingBalance(base.getId(), equipmentType.getId());
        int initialActiveAssigned = inventoryStockService.getActiveAssignedAssets(base.getId(), equipmentType.getId());
        int initialAvailableStock = inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId());

        assertTrue(initialAvailableStock >= 5, "Sufficient stock required for test");

        // 1. Create Assignment of 5 units
        AssignmentCreateDTO assignDto = new AssignmentCreateDTO(
                base.getId(), equipmentType.getId(), "Lt. Dan Taylor", "Lieutenant", "MIL-5501", 5, LocalDateTime.now()
        );
        AssignmentResponseDTO assignment = assignmentService.createAssignment(assignDto, adminPrincipal);

        assertEquals(AssignmentStatus.ACTIVE, assignment.getStatus());
        assertEquals(initialClosingBalance, inventoryStockService.getClosingBalance(base.getId(), equipmentType.getId()));
        assertEquals(initialActiveAssigned + 5, inventoryStockService.getActiveAssignedAssets(base.getId(), equipmentType.getId()));
        assertEquals(initialAvailableStock - 5, inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId()));

        int postAssignAvailable = inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId());

        // 2. Expend 2 units from the 5 assigned units
        ExpenditureCreateDTO expDto = new ExpenditureCreateDTO(
                base.getId(), equipmentType.getId(), assignment.getId(), 2, "Combat damage during patrol", LocalDateTime.now()
        );
        ExpenditureResponseDTO expenditure = expenditureService.createExpenditure(expDto, adminPrincipal);

        assertNotNull(expenditure.getId());
        assertEquals(assignment.getId(), expenditure.getAssignmentId());

        // Closing balance must drop by 2
        assertEquals(initialClosingBalance - 2, inventoryStockService.getClosingBalance(base.getId(), equipmentType.getId()));

        // Active assigned assets must drop by 2 (5 - 2 = 3)
        assertEquals(initialActiveAssigned + 3, inventoryStockService.getActiveAssignedAssets(base.getId(), equipmentType.getId()));

        // Available stock must remain invariant (postAssignAvailable) - no double counting!
        assertEquals(postAssignAvailable, inventoryStockService.getAvailableStock(base.getId(), equipmentType.getId()));

        // 3. Attempting to expend 4 units from assignment (which only has 3 remaining active assigned) must be rejected
        ExpenditureCreateDTO excessiveExpDto = new ExpenditureCreateDTO(
                base.getId(), equipmentType.getId(), assignment.getId(), 4, "Excessive linked expenditure test", LocalDateTime.now()
        );
        assertThrows(InsufficientStockException.class, () -> expenditureService.createExpenditure(excessiveExpDto, adminPrincipal));

        // 4. Expend remaining 3 units from the assignment -> status must transition to EXPENDED
        ExpenditureCreateDTO finalExpDto = new ExpenditureCreateDTO(
                base.getId(), equipmentType.getId(), assignment.getId(), 3, "Complete loss of remaining assigned unit", LocalDateTime.now()
        );
        expenditureService.createExpenditure(finalExpDto, adminPrincipal);

        // Active assigned assets from this assignment is now 0
        assertEquals(initialActiveAssigned, inventoryStockService.getActiveAssignedAssets(base.getId(), equipmentType.getId()));
        assertEquals(initialClosingBalance - 5, inventoryStockService.getClosingBalance(base.getId(), equipmentType.getId()));
    }
}
