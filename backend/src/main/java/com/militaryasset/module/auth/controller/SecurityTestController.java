package com.militaryasset.module.auth.controller;

import com.militaryasset.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class SecurityTestController {

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminOnly() {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted"));
    }

    @GetMapping("/logistics-scope")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<String>> logisticsScope() {
        return ResponseEntity.ok(ApiResponse.success("Logistics scope access granted"));
    }

    @GetMapping("/assignment-scope")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<String>> assignmentScope() {
        return ResponseEntity.ok(ApiResponse.success("Assignment scope access granted (Logistics Officer denied)"));
    }

    @GetMapping("/base/{baseId}")
    @PreAuthorize("@baseSecurityService.isAccessAllowedForBase(#baseId)")
    public ResponseEntity<ApiResponse<String>> baseAccess(@PathVariable Long baseId) {
        return ResponseEntity.ok(ApiResponse.success("Access granted for base ID: " + baseId));
    }

    @PostMapping("/transfer-auth")
    @PreAuthorize("@baseSecurityService.isTransferAllowed(#sourceBaseId, #destinationBaseId)")
    public ResponseEntity<ApiResponse<String>> transferAuth(
            @RequestParam Long sourceBaseId,
            @RequestParam Long destinationBaseId) {
        return ResponseEntity.ok(ApiResponse.success("Transfer authorization granted from Base " + sourceBaseId + " to Base " + destinationBaseId));
    }
}
