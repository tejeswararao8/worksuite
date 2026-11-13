package com.platform.hr.platform.employees;

import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.MembershipValidationService;
import com.platform.tenancy.membership.UserTenantMembership;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import com.platform.security.user.KeycloakUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformEmployeeService")
class PlatformEmployeeServiceTest {

    @Mock private UserTenantMembershipRepository membershipRepository;
    @Mock private MembershipValidationService membershipValidationService;
    @Mock private PlatformInvitationRepository invitationRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private KeycloakUserService keycloakUserService;

    @InjectMocks
    private PlatformEmployeeService service;

    private static final UUID MEMBERSHIP_ID = UUID.randomUUID();
    private static final UUID PLATFORM_ID   = TenantSecurityService.PLATFORM_TENANT_ID;
    private static final String ACTOR_ID    = UUID.randomUUID().toString();

    @BeforeEach
    void setupSuperAdminContext() {
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR_ID);

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);
        lenient().when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN)));

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private UserTenantMembership platformMembership(MembershipStatus status, String role) {
        Tenant t = new Tenant();
        t.setId(PLATFORM_ID);
        return UserTenantMembership.builder()
                .id(MEMBERSHIP_ID)
                .keycloakUserId(UUID.randomUUID().toString())
                .tenant(t)
                .roleName(role)
                .membershipStatus(status)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    // ── 1. Invite with valid role ─────────────────────────────────────────────

    @Test
    @DisplayName("invite() creates PENDING invitation for valid platform role")
    void invite_validRole_createsPending() {
        InviteRequest req = new InviteRequest();
        req.setEmail("new@platform.com");
        req.setFirstName("New");
        req.setLastName("Admin");
        req.setRoleName(PlatformRoles.PLATFORM_ADMIN);

        when(invitationRepository.existsByEmailAndStatus(
                eq("new@platform.com"), eq(PlatformInvitation.InvitationStatus.PENDING)))
                .thenReturn(false);

        PlatformInvitation saved = PlatformInvitation.builder()
                .id(UUID.randomUUID())
                .email("new@platform.com")
                .firstName("New").lastName("Admin")
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .token("tok")
                .status(PlatformInvitation.InvitationStatus.PENDING)
                .invitedBy(ACTOR_ID)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        when(invitationRepository.save(any())).thenReturn(saved);

        InvitationDto result = service.invite(req);

        assertThat(result.getStatus()).isEqualTo(PlatformInvitation.InvitationStatus.PENDING);
        assertThat(result.getRoleName()).isEqualTo(PlatformRoles.PLATFORM_ADMIN);
    }

    // ── 2. Invite with invalid role is rejected ───────────────────────────────

    @Test
    @DisplayName("invite() rejects invalid role")
    void invite_invalidRole_throws() {
        InviteRequest req = new InviteRequest();
        req.setEmail("x@x.com");
        req.setFirstName("X"); req.setLastName("X");
        req.setRoleName("HR_ADMIN"); // not a platform role

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid platform role");
    }

    // ── 3. Non-super-admin cannot grant PLATFORM_SUPER_ADMIN ─────────────────

    @Test
    @DisplayName("invite() prevents privilege escalation — PLATFORM_ADMIN cannot grant SUPER_ADMIN")
    void invite_privilegeEscalation_denied() {
        // Override context to PLATFORM_ADMIN (not SUPER_ADMIN)
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)));
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        InviteRequest req = new InviteRequest();
        req.setEmail("x@x.com");
        req.setFirstName("X"); req.setLastName("X");
        req.setRoleName(PlatformRoles.PLATFORM_SUPER_ADMIN);

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Only PLATFORM_SUPER_ADMIN can grant");
    }

    // ── 4. Duplicate pending invitation is rejected ───────────────────────────

    @Test
    @DisplayName("invite() rejects duplicate pending invitation for same email")
    void invite_duplicatePending_throws() {
        InviteRequest req = new InviteRequest();
        req.setEmail("dup@platform.com");
        req.setFirstName("D"); req.setLastName("D");
        req.setRoleName(PlatformRoles.PLATFORM_SUPPORT);

        when(invitationRepository.existsByEmailAndStatus(
                eq("dup@platform.com"), eq(PlatformInvitation.InvitationStatus.PENDING)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("pending invitation already exists");
    }

    // ── 5. Suspend ACTIVE employee ────────────────────────────────────────────

    @Test
    @DisplayName("suspendEmployee() suspends an ACTIVE platform employee")
    void suspend_active_succeeds() {
        UserTenantMembership m = platformMembership(MembershipStatus.ACTIVE, PlatformRoles.PLATFORM_ADMIN);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Ensure there's another super admin so the last-super-admin check doesn't apply
        // (this employee is PLATFORM_ADMIN, not SUPER_ADMIN, so no check needed)

        PlatformEmployeeDto result = service.suspendEmployee(MEMBERSHIP_ID);

        assertThat(result.getMembershipStatus()).isEqualTo(MembershipStatus.SUSPENDED);
    }

    // ── 6. Reactivate SUSPENDED employee ─────────────────────────────────────

    @Test
    @DisplayName("reactivateEmployee() reactivates a SUSPENDED platform employee")
    void reactivate_suspended_succeeds() {
        UserTenantMembership m = platformMembership(MembershipStatus.SUSPENDED, PlatformRoles.PLATFORM_SUPPORT);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlatformEmployeeDto result = service.reactivateEmployee(MEMBERSHIP_ID);

        assertThat(result.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    // ── 7. Cannot suspend already-suspended employee ──────────────────────────

    @Test
    @DisplayName("suspendEmployee() throws if already SUSPENDED")
    void suspend_alreadySuspended_throws() {
        UserTenantMembership m = platformMembership(MembershipStatus.SUSPENDED, PlatformRoles.PLATFORM_ADMIN);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.suspendEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only ACTIVE memberships can be suspended");
    }

    // ── 8. Last SUPER_ADMIN cannot be suspended ───────────────────────────────

    @Test
    @DisplayName("suspendEmployee() prevents suspending the last PLATFORM_SUPER_ADMIN")
    void suspend_lastSuperAdmin_throws() {
        String superAdminId = UUID.randomUUID().toString();
        Tenant t = new Tenant(); t.setId(PLATFORM_ID);
        UserTenantMembership m = UserTenantMembership.builder()
                .id(MEMBERSHIP_ID).keycloakUserId(superAdminId).tenant(t)
                .roleName(PlatformRoles.PLATFORM_SUPER_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        // Only one SUPER_ADMIN — the one being suspended
        when(membershipRepository.findByTenantIdAndMembershipStatus(PLATFORM_ID, MembershipStatus.ACTIVE))
                .thenReturn(List.of(m));

        assertThatThrownBy(() -> service.suspendEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("last active PLATFORM_SUPER_ADMIN");
    }

    // ── 9. Role change validates allowlist ────────────────────────────────────

    @Test
    @DisplayName("changeRole() rejects non-platform role")
    void changeRole_invalidRole_throws() {
        // Role validation happens before DB lookup — no stub needed
        assertThatThrownBy(() -> service.changeRole(MEMBERSHIP_ID, "TENANT_ADMIN"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid platform role");
    }

    // ── 10. Non-platform membership is rejected ───────────────────────────────

    @Test
    @DisplayName("getEmployee() rejects membership from a different tenant")
    void getEmployee_wrongTenant_throws() {
        Tenant other = new Tenant();
        other.setId(UUID.randomUUID()); // not the platform tenant
        UserTenantMembership m = UserTenantMembership.builder()
                .id(MEMBERSHIP_ID).keycloakUserId("x").tenant(other)
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.getEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("platform tenant");
    }

    // ── 11. Revoke invitation — only PENDING ──────────────────────────────────

    @Test
    @DisplayName("revokeInvitation() rejects revoking an ACCEPTED invitation")
    void revokeInvitation_accepted_throws() {
        PlatformInvitation inv = PlatformInvitation.builder()
                .id(UUID.randomUUID())
                .status(PlatformInvitation.InvitationStatus.ACCEPTED)
                .build();
        when(invitationRepository.findById(any())).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.revokeInvitation(inv.getId()))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only PENDING invitations can be revoked");
    }

    // ── 12. Revoke employee — last super admin protection ─────────────────────

    @Test
    @DisplayName("revokeEmployee() prevents revoking the last PLATFORM_SUPER_ADMIN")
    void revokeEmployee_lastSuperAdmin_throws() {
        String superAdminId = UUID.randomUUID().toString();
        Tenant t = new Tenant(); t.setId(PLATFORM_ID);
        UserTenantMembership m = UserTenantMembership.builder()
                .id(MEMBERSHIP_ID).keycloakUserId(superAdminId).tenant(t)
                .roleName(PlatformRoles.PLATFORM_SUPER_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.findByTenantIdAndMembershipStatus(PLATFORM_ID, MembershipStatus.ACTIVE))
                .thenReturn(List.of(m));

        assertThatThrownBy(() -> service.revokeEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("last active PLATFORM_SUPER_ADMIN");
    }
}
