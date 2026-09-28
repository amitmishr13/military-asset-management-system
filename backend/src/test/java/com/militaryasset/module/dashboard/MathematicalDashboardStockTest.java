package com.militaryasset.module.dashboard;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.repository.AssignmentRepository;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.dashboard.dto.DashboardMetricsDTO;
import com.militaryasset.module.dashboard.service.DashboardService;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MathematicalDashboardStockTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

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

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Verify Exact Mathematical Consistency: Net Movement = 25, Closing = 122, Available = 114")
    public void testExactMathematicalDashboardCalculations() {
        // 1. Create Isolated Base and Equipment
        Base testBase = baseRepository.save(Base.builder().code("MATH_BASE").name("Math Verification Base").location("Zone M").build());
        Base otherBase = baseRepository.save(Base.builder().code("OTHER_BASE").name("Other Base").location("Zone O").build());
        EquipmentType testEquip = equipmentTypeRepository.save(EquipmentType.builder().code("MATH_EQUIP").name("Math Verification Gear").category("GEAR").unitOfMeasure("UNITS").build());

        User adminUser = userRepository.findByUsername("admin").orElseThrow();
        CustomUserDetails adminPrincipal = new CustomUserDetails(adminUser);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities()));

        LocalDate testDate = LocalDate.of(2026, 5, 15);
        LocalDateTime testDateTime = LocalDateTime.of(2026, 5, 15, 12, 0);

        // Initial Inventory = 100 as of date
        initialInventoryRepository.save(InitialInventory.builder().base(testBase).equipmentType(testEquip).initialQuantity(100).asOfDate(LocalDate.of(2026, 1, 1)).build());

        // Purchases on testDate = 20
        purchaseRepository.save(Purchase.builder().purchaseReference("PUR-MATH-01").base(testBase).equipmentType(testEquip).quantity(20).unitCost(BigDecimal.TEN).purchaseDate(testDateTime).recordedByUser(adminUser).build());

        // Transfer In on testDate = 10
        transferRepository.save(Transfer.builder().transferReference("TRN-MATH-IN").sourceBase(otherBase).destinationBase(testBase).equipmentType(testEquip).quantity(10).transferDate(testDateTime).initiatedByUser(adminUser).build());

        // Transfer Out on testDate = 5
        transferRepository.save(Transfer.builder().transferReference("TRN-MATH-OUT").sourceBase(testBase).destinationBase(otherBase).equipmentType(testEquip).quantity(5).transferDate(testDateTime).initiatedByUser(adminUser).build());

        // Expenditure on testDate = 3
        expenditureRepository.save(Expenditure.builder().expenditureReference("EXP-MATH-01").base(testBase).equipmentType(testEquip).expendedQuantity(3).reason("Testing").expendedDate(testDateTime).recordedByUser(adminUser).build());

        // Active Assignment on testDate = 8
        assignmentRepository.save(Assignment.builder().assignmentReference("ASN-MATH-01").base(testBase).equipmentType(testEquip).personnelName("Officer Test").personnelId("T-100").assignedQuantity(8).status(AssignmentStatus.ACTIVE).assignedDate(testDateTime).assignedByUser(adminUser).build());

        // 2. Fetch Dashboard Metrics
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics(testDate, testBase.getId(), testEquip.getId(), adminPrincipal);

        // 3. Mathematical Verifications
        assertEquals(100, metrics.getOpeningBalance(), "Opening balance must equal initial quantity (100)");
        assertEquals(20, metrics.getPurchases(), "Purchases on test date must equal 20");
        assertEquals(10, metrics.getTransferIn(), "Transfer In on test date must equal 10");
        assertEquals(5, metrics.getTransferOut(), "Transfer Out on test date must equal 5");

        // Net Movement = 20 + 10 - 5 = 25
        assertEquals(25, metrics.getNetMovement(), "Net Movement must equal 20 + 10 - 5 = 25");

        assertEquals(3, metrics.getExpendedAssets(), "Expended assets on test date must equal 3");

        // Closing Balance = 100 + 25 - 3 = 122
        assertEquals(122, metrics.getClosingBalance(), "Closing Balance must equal 100 + 25 - 3 = 122");

        assertEquals(8, metrics.getActiveAssignedAssets(), "Active Assigned Assets must equal 8");

        // Available Stock = 122 - 8 = 114
        assertEquals(114, metrics.getAvailableStock(), "Available Stock must equal 122 - 8 = 114");
    }
}
