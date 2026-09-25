package com.hrms.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    public enum AuditAction {
        CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT, EXPORT, IMPORT, APPROVE, REJECT
    }
}
