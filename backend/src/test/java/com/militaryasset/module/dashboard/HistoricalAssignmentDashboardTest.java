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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HistoricalAssignmentDashboardTest {

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
    private AssignmentRepository assignmentRepository;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Verify Point-In-Time Historical Assignment Metrics Across Date 1 vs Date 2")
    public void testHistoricalAssignmentPointInTimeReconstruction() {
        Base base = baseRepository.save(Base.builder().code("HIST_BASE").name("Historical Base").location("Sector H").build());
        EquipmentType equip = equipmentTypeRepository.save(EquipmentType.builder().code("HIST_EQUIP").name("Historical Gear").category("GEAR").unitOfMeasure("UNITS").build());

        User adminUser = userRepository.findByUsername("admin").orElseThrow();
        CustomUserDetails adminPrincipal = new CustomUserDetails(adminUser);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities()));

        initialInventoryRepository.save(InitialInventory.builder().base(base).equipmentType(equip).initialQuantity(50).asOfDate(LocalDate.of(2026, 1, 1)).build());

        LocalDate date0 = LocalDate.of(2026, 5, 1);
        LocalDate date1 = LocalDate.of(2026, 5, 10);
        LocalDate date2 = LocalDate.of(2026, 5, 20);

        // Assign 10 units on Date 1 (2026-05-10) and return them on Date 2 (2026-05-20)
        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .assignmentReference("ASN-HIST-01")
                .base(base)
                .equipmentType(equip)
                .personnelName("Lt. Col. John C")
                .personnelId("MIL-001")
                .assignedQuantity(10)
                .status(AssignmentStatus.RETURNED)
                .assignedDate(LocalDateTime.of(2026, 5, 10, 9, 0))
                .returnedDate(LocalDateTime.of(2026, 5, 20, 17, 0))
                .assignedByUser(adminUser)
                .build());

        // 1. Dashboard on Date 0 (Before Assignment)
        DashboardMetricsDTO d0Metrics = dashboardService.getDashboardMetrics(date0, base.getId(), equip.getId(), adminPrincipal);
        assertEquals(0, d0Metrics.getActiveAssignedAssets(), "Active assigned assets on Date 0 (before creation) must be 0");
        assertEquals(50, d0Metrics.getAvailableStock(), "Available stock on Date 0 must be 50");

        // 2. Dashboard on Date 1 (While Assignment WAS Active)
        DashboardMetricsDTO d1Metrics = dashboardService.getDashboardMetrics(date1, base.getId(), equip.getId(), adminPrincipal);
        assertEquals(10, d1Metrics.getActiveAssignedAssets(), "Active assigned assets on Date 1 (while active) must be 10");
        assertEquals(40, d1Metrics.getAvailableStock(), "Available stock on Date 1 must be 50 - 10 = 40");

        // 3. Dashboard on Date 2 (After Assignment was Returned)
        DashboardMetricsDTO d2Metrics = dashboardService.getDashboardMetrics(date2, base.getId(), equip.getId(), adminPrincipal);
        assertEquals(0, d2Metrics.getActiveAssignedAssets(), "Active assigned assets on Date 2 (after return) must be 0");
        assertEquals(50, d2Metrics.getAvailableStock(), "Available stock on Date 2 must be 50");
    }
}
