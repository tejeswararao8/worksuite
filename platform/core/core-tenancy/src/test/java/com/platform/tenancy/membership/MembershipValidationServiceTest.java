package com.platform.tenancy.membership;

import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipValidationService")
class MembershipValidationServiceTest {

    @Mock
    private UserTenantMembershipRepository membershipRepository;

    @InjectMocks
    private MembershipValidationService service;

    private static final String USER_ID   = UUID.randomUUID().toString();
    private static final UUID   TENANT_ID = UUID.randomUUID();

    private UserTenantMembership membershipWithStatus(MembershipStatus status) {
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        return UserTenantMembership.builder()
                .id(UUID.randomUUID())
                .keycloakUserId(USER_ID)
                .tenant(tenant)
                .roleName("HR_MANAGER")
                .membershipStatus(status)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    // ── 1. Active membership grants access ───────────────────────────────────

    @Test
    @DisplayName("ACTIVE membership passes validation")
    void activeMembership_passes() {
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(membershipWithStatus(MembershipStatus.ACTIVE)));

        assertThatNoException()
                .isThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()));
    }

    // ── 2. Pending membership is rejected ────────────────────────────────────

    @Test
    @DisplayName("PENDING membership is rejected")
    void pendingMembership_rejected() {
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(membershipWithStatus(MembershipStatus.PENDING)));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("pending");
    }

    // ── 3. Suspended membership is rejected ──────────────────────────────────

    @Test
    @DisplayName("SUSPENDED membership is rejected")
    void suspendedMembership_rejected() {
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(membershipWithStatus(MembershipStatus.SUSPENDED)));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("suspended");
    }

    // ── 4. Revoked membership is rejected ────────────────────────────────────

    @Test
    @DisplayName("REVOKED membership is rejected")
    void revokedMembership_rejected() {
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(membershipWithStatus(MembershipStatus.REVOKED)));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("revoked");
    }

    // ── 5. No membership at all is rejected ──────────────────────────────────

    @Test
    @DisplayName("No membership in tenant is rejected")
    void noMembership_rejected() {
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("no membership");
    }

    // ── 6. Platform tenant membership is enforced (no bypass) ─────────────────

    @Test
    @DisplayName("Platform pseudo-tenant: ACTIVE membership is required — no bypass")
    void platformTenant_activeMembershipRequired() {
        String platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID.toString();
        UUID   platformUuid     = TenantSecurityService.PLATFORM_TENANT_ID;

        Tenant platformTenant = new Tenant();
        platformTenant.setId(platformUuid);
        UserTenantMembership active = UserTenantMembership.builder()
                .id(UUID.randomUUID())
                .keycloakUserId(USER_ID)
                .tenant(platformTenant)
                .roleName("PLATFORM_SUPER_ADMIN")
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, platformUuid))
                .thenReturn(Optional.of(active));

        // ACTIVE platform membership must pass
        assertThatNoException()
                .isThrownBy(() -> service.validateActiveMembership(USER_ID, platformTenantId));

        // Repository MUST be called — no bypass
        verify(membershipRepository).findByKeycloakUserIdAndTenantId(USER_ID, platformUuid);
    }

    @Test
    @DisplayName("Platform pseudo-tenant: missing membership is denied")
    void platformTenant_missingMembership_denied() {
        String platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID.toString();
        UUID   platformUuid     = TenantSecurityService.PLATFORM_TENANT_ID;

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, platformUuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, platformTenantId))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("no membership");
    }

    @Test
    @DisplayName("Platform pseudo-tenant: PENDING membership is denied")
    void platformTenant_pendingMembership_denied() {
        String platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID.toString();
        UUID   platformUuid     = TenantSecurityService.PLATFORM_TENANT_ID;

        Tenant platformTenant = new Tenant();
        platformTenant.setId(platformUuid);
        UserTenantMembership pending = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(platformTenant)
                .roleName("PLATFORM_SUPER_ADMIN")
                .membershipStatus(MembershipStatus.PENDING).build();

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, platformUuid))
                .thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, platformTenantId))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("pending");
    }

    @Test
    @DisplayName("Platform pseudo-tenant: SUSPENDED membership is denied")
    void platformTenant_suspendedMembership_denied() {
        String platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID.toString();
        UUID   platformUuid     = TenantSecurityService.PLATFORM_TENANT_ID;

        Tenant platformTenant = new Tenant();
        platformTenant.setId(platformUuid);
        UserTenantMembership suspended = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(platformTenant)
                .roleName("PLATFORM_SUPER_ADMIN")
                .membershipStatus(MembershipStatus.SUSPENDED).build();

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, platformUuid))
                .thenReturn(Optional.of(suspended));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, platformTenantId))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    @DisplayName("Platform pseudo-tenant: REVOKED membership is denied")
    void platformTenant_revokedMembership_denied() {
        String platformTenantId = TenantSecurityService.PLATFORM_TENANT_ID.toString();
        UUID   platformUuid     = TenantSecurityService.PLATFORM_TENANT_ID;

        Tenant platformTenant = new Tenant();
        platformTenant.setId(platformUuid);
        UserTenantMembership revoked = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(platformTenant)
                .roleName("PLATFORM_SUPER_ADMIN")
                .membershipStatus(MembershipStatus.REVOKED).build();

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, platformUuid))
                .thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, platformTenantId))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("revoked");
    }

    // ── 7. Unauthorized tenant switching is rejected ──────────────────────────
    // A user with ACTIVE membership in tenant A cannot access tenant B.

    @Test
    @DisplayName("User with membership in tenant A cannot access tenant B")
    void unauthorizedTenantSwitch_rejected() {
        UUID tenantB = UUID.randomUUID();
        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, tenantB))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, tenantB.toString()))
                .isInstanceOf(TenantSecurityException.class);
    }

    // ── 8. User with multiple tenant memberships ──────────────────────────────

    @Test
    @DisplayName("User with multiple ACTIVE memberships can access each tenant")
    void multiTenantUser_canAccessEachTenant() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        Tenant tA = new Tenant(); tA.setId(tenantA);
        Tenant tB = new Tenant(); tB.setId(tenantB);

        UserTenantMembership mA = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(tA)
                .roleName("HR_ADMIN").membershipStatus(MembershipStatus.ACTIVE).build();
        UserTenantMembership mB = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(tB)
                .roleName("EMPLOYEE").membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findActiveMembershipsByUser(USER_ID))
                .thenReturn(List.of(mA, mB));

        List<UserTenantMembership> memberships = service.getActiveMemberships(USER_ID);
        assertThat(memberships).hasSize(2);
        assertThat(memberships).extracting(m -> m.getTenant().getId())
                .containsExactlyInAnyOrder(tenantA, tenantB);
    }

    // ── 9. Platform role separation — platform role cannot be in tenant membership ─

    @Test
    @DisplayName("Membership with platform role is still validated by status (role is informational)")
    void platformRoleInMembership_statusStillEnforced() {
        // Even if someone manually inserted a platform role into a membership,
        // the status check still applies — PENDING is rejected regardless of role.
        Tenant tenant = new Tenant(); tenant.setId(TENANT_ID);
        UserTenantMembership m = UserTenantMembership.builder()
                .keycloakUserId(USER_ID).tenant(tenant)
                .roleName("PLATFORM_SUPER_ADMIN") // should not be here, but status still enforced
                .membershipStatus(MembershipStatus.PENDING).build();

        when(membershipRepository.findByKeycloakUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.validateActiveMembership(USER_ID, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("pending");
    }

    // ── 10. Cross-tenant resource access — null userId rejected ──────────────

    @Test
    @DisplayName("Null userId is rejected before any DB lookup")
    void nullUserId_rejected() {
        assertThatThrownBy(() -> service.validateActiveMembership(null, TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class);

        verify(membershipRepository, never()).findByKeycloakUserIdAndTenantId(any(), any());
    }
}
