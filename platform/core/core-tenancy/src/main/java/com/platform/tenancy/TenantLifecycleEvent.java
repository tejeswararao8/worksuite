package com.platform.tenancy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable record of a single tenant status transition.
 * Never updated or deleted — append-only audit trail.
 */
@Entity
@Table(name = "tenant_lifecycle_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantLifecycleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /** Keycloak subject UUID of the platform admin who performed the action. */
    @Column(name = "actor_id", nullable = false, length = 255)
    private String actorId;

    @Column(name = "actor_role", nullable = false, length = 100)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 50)
    private Tenant.TenantStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private Tenant.TenantStatus toStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void prePersist() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }
}
