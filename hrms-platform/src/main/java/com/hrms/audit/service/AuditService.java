package com.hrms.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.entity.AuditLog.AuditAction;
import com.hrms.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void log(UUID companyId, String entityName, String entityId,
                    AuditAction action, Object oldValue, Object newValue,
                    String performedBy, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .companyId(companyId)
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .performedBy(performedBy)
                    .performedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Failed to save audit log for entity={}, action={}", entityName, action, ex);
        }
    }
}
