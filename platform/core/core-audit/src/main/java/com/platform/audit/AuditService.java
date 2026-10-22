package com.platform.audit;

import com.platform.tenancy.TenantContext;
import com.platform.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String entityName, String entityId, AuditLog.AuditAction action,
                    String oldValue, String newValue, String ipAddress) {
        UUID companyId = UserPrincipal.currentCompanyId();
        UUID userId = UserPrincipal.currentUserId();

        AuditLog log = AuditLog.builder()
            .entityName(entityName)
            .entityId(entityId)
            .action(action)
            .oldValue(oldValue)
            .newValue(newValue)
            .performedBy(userId != null ? userId.toString() : "system")
            .ipAddress(ipAddress)
            .build();

        if (companyId != null) {
            log.setCompanyId(companyId);
        }

        auditLogRepository.save(log);
    }

    @Async
    public void log(String entityName, String entityId, AuditLog.AuditAction action) {
        log(entityName, entityId, action, null, null, null);
    }

    public Page<AuditLog> findByTenant(Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
    }

    public Page<AuditLog> findByEntity(String entityName, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return auditLogRepository.findByCompanyIdAndEntityNameOrderByCreatedAtDesc(companyId, entityName, pageable);
    }

    public Page<AuditLog> findByEntityId(String entityId, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return auditLogRepository.findByCompanyIdAndEntityIdOrderByCreatedAtDesc(companyId, entityId, pageable);
    }
}
