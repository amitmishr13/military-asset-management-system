package com.militaryasset.module.audit.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.audit.entity.AuditLog;
import com.militaryasset.module.audit.service.AuditLogService;
import com.militaryasset.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) Long baseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<AuditLog> logs = auditLogService.getAuditLogs(baseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }
}
