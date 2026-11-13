package com.platform.tenancy.membership;

import com.platform.tenancy.TenantSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Validates that a Keycloak user has an ACTIVE membership in the requested tenant.
 *
 * <p>This is the second gate after {@link TenantSecurityService} validates the tenant itself.
 * Even if the tenant is ACTIVE, a user without an ACTIVE membership must be rejected.
 *
 * <p>Platform users (companyId == PLATFORM_TENANT_ID) are subject to the same membership
 * check as all other users. A platform role in the JWT is not sufficient — the user must
 * also have an ACTIVE UserTenantMembership row in the platform pseudo-tenant. This prevents
 * a JWT with a forged or stale platform role from gaining access without a provisioned record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipValidationService {

    private final UserTenantMembershipRepository membershipRepository;

    /**
     * Validates that {@code keycloakUserId} has an ACTIVE membership in {@code tenantId}.
     * No tenant is exempt — the platform pseudo-tenant is validated identically to any other.
     *
     * @throws TenantSecurityException if no membership exists or membership is not ACTIVE
     */
    @Transactional(readOnly = true)
    public void validateActiveMembership(String keycloakUserId, String tenantId) {
        if (keycloakUserId == null || tenantId == null) {
            throw new TenantSecurityException("User identity or tenant missing from token");
        }

        UUID tenantUuid;
        try {
            tenantUuid = UUID.fromString(tenantId);
        } catch (IllegalArgumentException e) {
            throw new TenantSecurityException("Malformed tenant ID: " + tenantId);
        }

        UserTenantMembership membership = membershipRepository
                .findByKeycloakUserIdAndTenantId(keycloakUserId, tenantUuid)
                .orElseThrow(() -> {
                    log.warn("No membership found for user={} tenant={}", keycloakUserId, tenantId);
                    return new TenantSecurityException("User has no membership in this tenant");
                });

        switch (membership.getMembershipStatus()) {
            case ACTIVE -> { /* allowed */ }
            case PENDING -> {
                log.warn("Pending membership access attempt: user={} tenant={}", keycloakUserId, tenantId);
                throw new TenantSecurityException("Membership is pending acceptance");
            }
            case SUSPENDED -> {
                log.warn("Suspended membership access attempt: user={} tenant={}", keycloakUserId, tenantId);
                throw new TenantSecurityException("Membership is suspended");
            }
            case REVOKED -> {
                log.warn("Revoked membership access attempt: user={} tenant={}", keycloakUserId, tenantId);
                throw new TenantSecurityException("Membership has been revoked");
            }
        }
    }

    /**
     * Returns all tenants where the user has an ACTIVE membership.
     * Used by the tenant-selection endpoint so users can only see their own tenants.
     */
    @Transactional(readOnly = true)
    public List<UserTenantMembership> getActiveMemberships(String keycloakUserId) {
        return membershipRepository.findActiveMembershipsByUser(keycloakUserId);
    }

    /**
     * Creates a new membership. Idempotent — does not create duplicates.
     */
    @Transactional
    public UserTenantMembership createMembership(UserTenantMembership membership) {
        boolean exists = membershipRepository.existsByKeycloakUserIdAndTenantId(
                membership.getKeycloakUserId(), membership.getTenant().getId());
        if (exists) {
            return membershipRepository
                    .findByKeycloakUserIdAndTenantId(
                            membership.getKeycloakUserId(), membership.getTenant().getId())
                    .orElseThrow();
        }
        return membershipRepository.save(membership);
    }
}
