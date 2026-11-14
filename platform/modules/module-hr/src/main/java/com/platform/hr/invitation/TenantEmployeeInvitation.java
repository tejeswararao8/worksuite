package com.platform.hr.invitation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks a pending invitation for a tenant employee.
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Only the SHA-256 hash of the raw token is stored — raw token is never persisted.</li>
 *   <li>The role is stored on the invitation and taken from it on acceptance — never from the client.</li>
 *   <li>tenant_id is set from TenantContext on creation — never from a client parameter.</li>
 *   <li>A unique partial index prevents two PENDING invitations for the same employee.</li>
 *   <li>keycloak_user_id is set only when the invitation is accepted.</li>
 * </ul>
 */
@Entity
@Table(name = "tenant_employee_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEmployeeInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "employee_id", nullable = false, updatable = false)
    private UUID employeeId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * SHA-256 hex digest of the raw invitation token.
     * The raw token is NEVER stored — only this hash is persisted.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "invited_by", nullable = false, length = 255)
    private String invitedBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "keycloak_user_id", length = 36)
    private String keycloakUserId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, EXPIRED, REVOKED
    }
}
