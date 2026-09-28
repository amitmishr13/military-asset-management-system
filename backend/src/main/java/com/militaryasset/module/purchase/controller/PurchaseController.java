package com.militaryasset.module.purchase.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.purchase.dto.PurchaseCreateDTO;
import com.militaryasset.module.purchase.dto.PurchaseResponseDTO;
import com.militaryasset.module.purchase.service.PurchaseService;
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
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<PurchaseResponseDTO>> createPurchase(
            @Valid @RequestBody PurchaseCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        PurchaseResponseDTO response = purchaseService.createPurchase(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase recorded successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<List<PurchaseResponseDTO>>> getPurchases(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<PurchaseResponseDTO> response = purchaseService.getPurchases(baseId, equipmentTypeId, startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Historical purchases retrieved successfully", response));
    }
}
