package com.platform.hr.platform.employees;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks a pending invitation for a new platform employee.
 * The invitation is created by a platform admin and sent to the invitee.
 * When the invitee logs in for the first time, the invitation is accepted
 * and a UserTenantMembership is created in the platform pseudo-tenant.
 */
@Entity
@Table(name = "platform_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * SHA-256 hex digest of the raw invitation token.
     * The raw token is NEVER stored — only this hash is persisted.
     * Length 64 = SHA-256 hex output.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    /** Keycloak user ID of the platform admin who sent the invitation. */
    @Column(name = "invited_by", nullable = false, length = 255)
    private String invitedBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /** Set when the invitation is accepted and the Keycloak user is created. */
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
