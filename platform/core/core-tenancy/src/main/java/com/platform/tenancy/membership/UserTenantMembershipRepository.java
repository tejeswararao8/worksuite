package com.platform.tenancy.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTenantMembershipRepository extends JpaRepository<UserTenantMembership, UUID> {

    /**
     * Find a user's membership in a specific tenant.
     * Used by JwtAuthFilter on every authenticated request.
     */
    @Query("SELECT m FROM UserTenantMembership m WHERE m.keycloakUserId = :userId AND m.tenant.id = :tenantId")
    Optional<UserTenantMembership> findByKeycloakUserIdAndTenantId(
            @Param("userId") String keycloakUserId,
            @Param("tenantId") UUID tenantId);

    /**
     * List all ACTIVE memberships for a user — used for tenant-switching UI.
     */
    @Query("SELECT m FROM UserTenantMembership m WHERE m.keycloakUserId = :userId AND m.membershipStatus = 'ACTIVE'")
    List<UserTenantMembership> findActiveMembershipsByUser(@Param("userId") String keycloakUserId);

    /**
     * List all memberships for a tenant — used by tenant admin management.
     */
    List<UserTenantMembership> findByTenantIdAndMembershipStatus(UUID tenantId, MembershipStatus status);

    boolean existsByKeycloakUserIdAndTenantId(String keycloakUserId, UUID tenantId);

    long countByTenantIdAndMembershipStatus(UUID tenantId, MembershipStatus status);
}
