package com.militaryasset.module.assignment.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.assignment.dto.AssignmentCreateDTO;
import com.militaryasset.module.assignment.dto.AssignmentResponseDTO;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.service.AssignmentService;
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
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> createAssignment(
            @Valid @RequestBody AssignmentCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        AssignmentResponseDTO response = assignmentService.createAssignment(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset assigned successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<List<AssignmentResponseDTO>>> getAssignments(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) AssignmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<AssignmentResponseDTO> response = assignmentService.getAssignments(baseId, equipmentTypeId, status, startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Assignments retrieved successfully", response));
    }
}
