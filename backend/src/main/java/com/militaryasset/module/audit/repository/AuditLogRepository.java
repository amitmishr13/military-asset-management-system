package com.militaryasset.module.audit.repository;

import com.militaryasset.module.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByBaseId(Long baseId);
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByBaseIdOrderByTimestampDesc(Long baseId);
}
