package com.militaryasset.module.transfer.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.transfer.dto.TransferCreateDTO;
import com.militaryasset.module.transfer.dto.TransferResponseDTO;
import com.militaryasset.module.transfer.service.TransferService;
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
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> createTransfer(
            @Valid @RequestBody TransferCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TransferResponseDTO response = transferService.createTransfer(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer initiated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getTransfers(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<TransferResponseDTO> response = transferService.getTransfers(baseId, equipmentTypeId, startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Historical transfers retrieved successfully", response));
    }
}
