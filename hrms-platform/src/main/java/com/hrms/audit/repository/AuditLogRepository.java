package com.hrms.audit.repository;

import com.hrms.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByCompanyIdOrderByPerformedAtDesc(UUID companyId, Pageable pageable);

    Page<AuditLog> findByCompanyIdAndEntityNameOrderByPerformedAtDesc(
            UUID companyId, String entityName, Pageable pageable);

    Page<AuditLog> findByCompanyIdAndEntityIdOrderByPerformedAtDesc(
            UUID companyId, String entityId, Pageable pageable);
}
