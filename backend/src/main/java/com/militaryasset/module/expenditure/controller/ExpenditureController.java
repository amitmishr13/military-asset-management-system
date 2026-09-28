package com.militaryasset.module.expenditure.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.expenditure.dto.ExpenditureCreateDTO;
import com.militaryasset.module.expenditure.dto.ExpenditureResponseDTO;
import com.militaryasset.module.expenditure.service.ExpenditureService;
import com.militaryasset.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    @Autowired
    private ExpenditureService expenditureService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<ExpenditureResponseDTO>> createExpenditure(
            @Valid @RequestBody ExpenditureCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ExpenditureResponseDTO response = expenditureService.createExpenditure(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset expenditure recorded successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<List<ExpenditureResponseDTO>>> getExpenditures(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<ExpenditureResponseDTO> response = expenditureService.getExpenditures(baseId, equipmentTypeId, assignmentId, startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Expenditures retrieved successfully", response));
    }
}
