package com.platform.tenancy.membership;

import com.platform.tenancy.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records that a specific Keycloak user belongs to a specific tenant with a given role.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>Uses {@code keycloak_user_id} (JWT subject) as the user reference — no dependency
 *       on the platform {@code users} table, which may not exist for all Keycloak users.</li>
 *   <li>Does NOT extend {@code BaseEntity} — membership records are cross-tenant by nature
 *       and must not be scoped to a single {@code company_id}.</li>
 *   <li>The unique constraint on (keycloak_user_id, tenant_id) prevents duplicate memberships.</li>
 *   <li>{@code role_name} stores the Keycloak realm role string (e.g. "HR_MANAGER") so that
 *       membership and role are co-located without a join to the roles table on every request.</li>
 * </ul>
 */
@Entity
@Table(
    name = "user_tenant_memberships",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_membership_user_tenant",
        columnNames = {"keycloak_user_id", "tenant_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTenantMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Keycloak subject UUID — matches the {@code sub} claim in the JWT.
     * This is the stable, immutable identifier for a user across all tenants.
     */
    @Column(name = "keycloak_user_id", nullable = false, length = 36)
    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * The Keycloak realm role name assigned to this user within this tenant.
     * Must match a value in {@link com.platform.security.rbac.PlatformRoles}.
     */
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 20)
    @Builder.Default
    private MembershipStatus membershipStatus = MembershipStatus.PENDING;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

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
}
