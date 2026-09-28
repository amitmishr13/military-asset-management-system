package com.militaryasset.module.dashboard.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.dashboard.dto.DashboardMetricsDTO;
import com.militaryasset.module.dashboard.dto.NetMovementBreakdownDTO;
import com.militaryasset.module.dashboard.service.DashboardService;
import com.militaryasset.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getDashboardMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics(date, baseId, equipmentTypeId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully", metrics));
    }

    @GetMapping("/net-movement-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<NetMovementBreakdownDTO>> getNetMovementDetails(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        NetMovementBreakdownDTO breakdown = dashboardService.getNetMovementDetails(date, baseId, equipmentTypeId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Net movement breakdown retrieved successfully", breakdown));
    }
}
