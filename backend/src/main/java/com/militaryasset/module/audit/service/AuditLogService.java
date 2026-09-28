package com.militaryasset.module.audit.service;

import com.militaryasset.module.audit.entity.AuditLog;
import com.militaryasset.module.audit.repository.AuditLogRepository;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(Long userId, String username, String action, String entityType, Long entityId, Long baseId, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username != null ? username : "SYSTEM")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .baseId(baseId)
                .details(details)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .build();
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(Long baseId, CustomUserDetails currentUser) {
        if (currentUser.getRole() == Role.LOGISTICS_OFFICER) {
            throw new AccessDeniedException("Forbidden: Logistics Officers are not authorized to view audit logs");
        }

        if (currentUser.getRole() == Role.BASE_COMMANDER) {
            return auditLogRepository.findByBaseIdOrderByTimestampDesc(currentUser.getBaseId());
        }

        if (baseId != null) {
            return auditLogRepository.findByBaseIdOrderByTimestampDesc(baseId);
        }

        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
