package com.platform.tenancy.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTenantMembershipRepository extends JpaRepository<UserTenantMembership, UUID> {

    @Query("SELECT m FROM UserTenantMembership m WHERE m.keycloakUserId = :userId AND m.tenant.id = :tenantId")
    Optional<UserTenantMembership> findByKeycloakUserIdAndTenantId(
            @Param("userId") String keycloakUserId,
            @Param("tenantId") UUID tenantId);

    @Query("SELECT m FROM UserTenantMembership m WHERE m.keycloakUserId = :userId AND m.membershipStatus = 'ACTIVE'")
    List<UserTenantMembership> findActiveMembershipsByUser(@Param("userId") String keycloakUserId);

    List<UserTenantMembership> findByTenantIdAndMembershipStatus(UUID tenantId, MembershipStatus status);

    List<UserTenantMembership> findByTenantId(UUID tenantId);

    @Query("SELECT m FROM UserTenantMembership m WHERE m.tenant.id = :tenantId")
    Page<UserTenantMembership> findByTenantIdPageable(
            @Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT m FROM UserTenantMembership m WHERE m.tenant.id = :tenantId AND m.membershipStatus = :status")
    Page<UserTenantMembership> findByTenantIdAndMembershipStatusPageable(
            @Param("tenantId") UUID tenantId,
            @Param("status") MembershipStatus status,
            Pageable pageable);

    boolean existsByKeycloakUserIdAndTenantId(String keycloakUserId, UUID tenantId);

    long countByTenantIdAndMembershipStatus(UUID tenantId, MembershipStatus status);
}
