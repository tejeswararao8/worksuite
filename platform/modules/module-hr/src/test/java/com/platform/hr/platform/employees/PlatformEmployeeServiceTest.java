package com.platform.hr.platform.employees;

import com.platform.notification.NotificationService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlatformEmployeeService.
 *
 * <p>These are pure unit tests — no Spring context, no database.
 * All dependencies are mocked with Mockito.
 * Security context is set up manually via SecurityContextHolder.
 *
 * <p>Contrast with PlatformEmployeeSecurityIT which tests the full
 * Spring MVC security filter chain.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformEmployeeService — Unit Tests")
class PlatformEmployeeServiceTest {

    @Mock private UserTenantMembershipRepository membershipRepository;
    @Mock private MembershipValidationService membershipValidationService;
    @Mock private PlatformInvitationRepository invitationRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private KeycloakUserService keycloakUserService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PlatformEmployeeService service;

    private static final UUID MEMBERSHIP_ID = UUID.randomUUID();
    private static final UUID PLATFORM_ID   = TenantSecurityService.PLATFORM_TENANT_ID;
    private static final String ACTOR_ID    = UUID.randomUUID().toString();

    @BeforeEach
    void setupSuperAdminContext() {
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR_ID);
        lenient().when(jwt.getClaimAsString("email")).thenReturn("actor@platform.com");
        lenient().when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);

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

    private PlatformInvitation pendingInvitation(String email, String role) {
        return PlatformInvitation.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("Test").lastName("User")
                .roleName(role)
                .tokenHash("somehash")
                .status(PlatformInvitation.InvitationStatus.PENDING)
                .invitedBy(ACTOR_ID)
                .expiresAt(LocalDateTime.now().plusDays(7))
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
                .tokenHash("somehash")
                .status(PlatformInvitation.InvitationStatus.PENDING)
                .invitedBy(ACTOR_ID)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        when(invitationRepository.save(any())).thenReturn(saved);

        InvitationDto result = service.invite(req);

        assertThat(result.getStatus()).isEqualTo(PlatformInvitation.InvitationStatus.PENDING);
        assertThat(result.getRoleName()).isEqualTo(PlatformRoles.PLATFORM_ADMIN);
        // Verify email was sent (async via NotificationService)
        verify(notificationService).sendEmail(eq("new@platform.com"), anyString(), anyString());
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

    // ── 5. Accept valid invitation creates ACTIVE membership ──────────────────

    @Test
    @DisplayName("acceptInvitation() creates ACTIVE membership for valid token")
    void accept_validToken_createsActiveMembership() {
        String rawToken = "validrawtoken123";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = PlatformInvitation.builder()
                .id(UUID.randomUUID())
                .email("actor@platform.com")   // matches JWT email in @BeforeEach
                .firstName("Test").lastName("User")
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .tokenHash(hash)
                .status(PlatformInvitation.InvitationStatus.PENDING)
                .invitedBy("someadmin")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        Tenant platformTenant = new Tenant();
        platformTenant.setId(PLATFORM_ID);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(ACTOR_ID, PLATFORM_ID))
                .thenReturn(false);
        when(tenantRepository.findById(PLATFORM_ID)).thenReturn(Optional.of(platformTenant));
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PlatformEmployeeDto result = service.acceptInvitation(rawToken);

        assertThat(result.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(result.getRoleName()).isEqualTo(PlatformRoles.PLATFORM_ADMIN);
        assertThat(inv.getStatus()).isEqualTo(PlatformInvitation.InvitationStatus.ACCEPTED);
        assertThat(inv.getKeycloakUserId()).isEqualTo(ACTOR_ID);
    }

    // ── 6. Accept with invalid token is rejected ──────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects unknown token")
    void accept_unknownToken_throws() {
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptInvitation("badtoken"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid or unknown invitation token");
    }

    // ── 7. Accept revoked invitation is rejected ──────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects REVOKED invitation")
    void accept_revokedInvitation_throws() {
        String rawToken = "revokedtoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = pendingInvitation("actor@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);
        inv.setStatus(PlatformInvitation.InvitationStatus.REVOKED);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("revoked");
    }

    // ── 8. Accept already-accepted invitation is rejected ─────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects already ACCEPTED invitation (single-use)")
    void accept_alreadyAccepted_throws() {
        String rawToken = "usedtoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = pendingInvitation("actor@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);
        inv.setStatus(PlatformInvitation.InvitationStatus.ACCEPTED);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("already been accepted");
    }

    // ── 9. Accept expired invitation is rejected ──────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects expired invitation")
    void accept_expiredInvitation_throws() {
        String rawToken = "expiredtoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = pendingInvitation("actor@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);
        inv.setExpiresAt(LocalDateTime.now().minusDays(1)); // already expired

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("expired");

        assertThat(inv.getStatus()).isEqualTo(PlatformInvitation.InvitationStatus.EXPIRED);
    }

    // ── 10. Accept with wrong email is rejected ───────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects when JWT email does not match invitation email")
    void accept_emailMismatch_throws() {
        String rawToken = "mismatchtoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        // Invitation is for a different email than the JWT actor
        PlatformInvitation inv = pendingInvitation("other@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("does not match the invitation email");
    }

    // ── 11. Accept with unverified email is rejected ──────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects when email_verified is false")
    void accept_emailNotVerified_throws() {
        String rawToken = "unverifiedtoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = pendingInvitation("actor@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);

        // Override JWT to have email_verified = false
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR_ID);
        when(jwt.getClaimAsString("email")).thenReturn("actor@platform.com");
        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(false);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        lenient().when(auth.getAuthorities()).thenAnswer(i ->
                List.of(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_SUPER_ADMIN)));
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Email address must be verified");
    }

    // ── 12. Accept when membership already exists is rejected ─────────────────

    @Test
    @DisplayName("acceptInvitation() rejects when platform membership already exists")
    void accept_duplicateMembership_throws() {
        String rawToken = "duptoken";
        String hash = PlatformEmployeeService.hashToken(rawToken);

        PlatformInvitation inv = pendingInvitation("actor@platform.com", PlatformRoles.PLATFORM_ADMIN);
        inv.setTokenHash(hash);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(ACTOR_ID, PLATFORM_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("membership already exists");
    }

    // ── 13. Resend rotates token ──────────────────────────────────────────────

    @Test
    @DisplayName("resendInvitation() rotates the token hash")
    void resend_rotatesTokenHash() {
        PlatformInvitation inv = pendingInvitation("x@platform.com", PlatformRoles.PLATFORM_SUPPORT);
        String originalHash = inv.getTokenHash();

        when(invitationRepository.findById(inv.getId())).thenReturn(Optional.of(inv));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.resendInvitation(inv.getId());

        assertThat(inv.getTokenHash()).isNotEqualTo(originalHash);
        assertThat(inv.getStatus()).isEqualTo(PlatformInvitation.InvitationStatus.PENDING);
        verify(notificationService).sendEmail(eq("x@platform.com"), anyString(), anyString());
    }

    // ── 14. Suspend ACTIVE employee ───────────────────────────────────────────

    @Test
    @DisplayName("suspendEmployee() suspends an ACTIVE platform employee")
    void suspend_active_succeeds() {
        UserTenantMembership m = platformMembership(MembershipStatus.ACTIVE, PlatformRoles.PLATFORM_ADMIN);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlatformEmployeeDto result = service.suspendEmployee(MEMBERSHIP_ID);

        assertThat(result.getMembershipStatus()).isEqualTo(MembershipStatus.SUSPENDED);
    }

    // ── 15. Reactivate SUSPENDED employee ────────────────────────────────────

    @Test
    @DisplayName("reactivateEmployee() reactivates a SUSPENDED platform employee")
    void reactivate_suspended_succeeds() {
        UserTenantMembership m = platformMembership(MembershipStatus.SUSPENDED, PlatformRoles.PLATFORM_SUPPORT);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlatformEmployeeDto result = service.reactivateEmployee(MEMBERSHIP_ID);

        assertThat(result.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    // ── 16. Cannot suspend already-suspended employee ─────────────────────────

    @Test
    @DisplayName("suspendEmployee() throws if already SUSPENDED")
    void suspend_alreadySuspended_throws() {
        UserTenantMembership m = platformMembership(MembershipStatus.SUSPENDED, PlatformRoles.PLATFORM_ADMIN);
        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.suspendEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only ACTIVE memberships can be suspended");
    }

    // ── 17. Last SUPER_ADMIN cannot be suspended ──────────────────────────────

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
        when(membershipRepository.findByTenantIdAndMembershipStatus(PLATFORM_ID, MembershipStatus.ACTIVE))
                .thenReturn(List.of(m));

        assertThatThrownBy(() -> service.suspendEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("last active PLATFORM_SUPER_ADMIN");
    }

    // ── 18. Role change validates allowlist ───────────────────────────────────

    @Test
    @DisplayName("changeRole() rejects non-platform role")
    void changeRole_invalidRole_throws() {
        assertThatThrownBy(() -> service.changeRole(MEMBERSHIP_ID, "TENANT_ADMIN"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid platform role");
    }

    // ── 19. Non-platform membership is rejected ───────────────────────────────

    @Test
    @DisplayName("getEmployee() rejects membership from a different tenant")
    void getEmployee_wrongTenant_throws() {
        Tenant other = new Tenant();
        other.setId(UUID.randomUUID());
        UserTenantMembership m = UserTenantMembership.builder()
                .id(MEMBERSHIP_ID).keycloakUserId("x").tenant(other)
                .roleName(PlatformRoles.PLATFORM_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.getEmployee(MEMBERSHIP_ID))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("platform tenant");
    }

    // ── 20. Revoke invitation — only PENDING ──────────────────────────────────

    @Test
    @DisplayName("revokeInvitation() rejects revoking an ACCEPTED invitation")
    void revokeInvitation_accepted_throws() {
        PlatformInvitation inv = PlatformInvitation.builder()
                .id(UUID.randomUUID())
                .tokenHash("hash")
                .status(PlatformInvitation.InvitationStatus.ACCEPTED)
                .build();
        when(invitationRepository.findById(any())).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.revokeInvitation(inv.getId()))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only PENDING invitations can be revoked");
    }

    // ── 21. Revoke employee — last super admin protection ─────────────────────

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

    // ── 22. Last SUPER_ADMIN cannot be demoted ────────────────────────────────

    @Test
    @DisplayName("changeRole() prevents demoting the last PLATFORM_SUPER_ADMIN")
    void changeRole_lastSuperAdmin_throws() {
        String superAdminId = UUID.randomUUID().toString();
        Tenant t = new Tenant(); t.setId(PLATFORM_ID);
        UserTenantMembership m = UserTenantMembership.builder()
                .id(MEMBERSHIP_ID).keycloakUserId(superAdminId).tenant(t)
                .roleName(PlatformRoles.PLATFORM_SUPER_ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE).build();

        when(membershipRepository.findById(MEMBERSHIP_ID)).thenReturn(Optional.of(m));
        when(membershipRepository.findByTenantIdAndMembershipStatus(PLATFORM_ID, MembershipStatus.ACTIVE))
                .thenReturn(List.of(m));

        assertThatThrownBy(() -> service.changeRole(MEMBERSHIP_ID, PlatformRoles.PLATFORM_ADMIN))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("last active PLATFORM_SUPER_ADMIN");
    }

    // ── 23. hashToken produces 64-char SHA-256 hex ────────────────────────────

    @Test
    @DisplayName("hashToken() produces a 64-character SHA-256 hex digest")
    void hashToken_produces64CharHex() {
        String hash = PlatformEmployeeService.hashToken("anytoken");
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }

    // ── 24. hashToken is deterministic ───────────────────────────────────────

    @Test
    @DisplayName("hashToken() is deterministic for the same input")
    void hashToken_isDeterministic() {
        String h1 = PlatformEmployeeService.hashToken("sametoken");
        String h2 = PlatformEmployeeService.hashToken("sametoken");
        assertThat(h1).isEqualTo(h2);
    }

    // ── 25. hashToken produces different hashes for different tokens ──────────

    @Test
    @DisplayName("hashToken() produces different hashes for different tokens")
    void hashToken_differentInputsDifferentHashes() {
        String h1 = PlatformEmployeeService.hashToken("token1");
        String h2 = PlatformEmployeeService.hashToken("token2");
        assertThat(h1).isNotEqualTo(h2);
    }
}
