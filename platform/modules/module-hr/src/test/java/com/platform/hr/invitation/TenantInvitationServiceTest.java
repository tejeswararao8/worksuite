package com.platform.hr.invitation;

import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.notification.NotificationService;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.UserTenantMembership;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantInvitationService — Unit Tests")
class TenantInvitationServiceTest {

    @Mock private TenantEmployeeInvitationRepository invitationRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserTenantMembershipRepository membershipRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private NotificationService notificationService;
    @Mock private SubscriptionService subscriptionService;

    @InjectMocks
    private TenantInvitationService service;

    private static final UUID TENANT_ID  = UUID.randomUUID();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final String ACTOR_ID  = UUID.randomUUID().toString();
    private static final String EMP_EMAIL = "emp@company.com";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "invitationExpiryDays", 7);
        TenantContext.setTenantId(TENANT_ID.toString());

        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR_ID);
        lenient().when(jwt.getClaimAsString("email")).thenReturn(EMP_EMAIL);
        lenient().when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    // ── Helper builders ───────────────────────────────────────────────────────

    private Employee activeEmployee() {
        Employee e = new Employee();
        e.setId(EMPLOYEE_ID);
        e.setCompanyId(TENANT_ID);
        e.setEmail(EMP_EMAIL);
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEmployeeCode("EMP00001");
        e.setJoiningDate(LocalDate.now());
        e.setEmploymentStatus(EmploymentStatus.PROBATION);
        e.setIsActive(true);
        return e;
    }

    private TenantEmployeeInvitation pendingInvitation(String tokenHash) {
        return TenantEmployeeInvitation.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .employeeId(EMPLOYEE_ID)
                .email(EMP_EMAIL)
                .roleName(PlatformRoles.EMPLOYEE)
                .tokenHash(tokenHash)
                .status(TenantEmployeeInvitation.InvitationStatus.PENDING)
                .invitedBy(ACTOR_ID)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    // ── 1. Successful invitation ──────────────────────────────────────────────

    @Test
    @DisplayName("invite() creates PENDING invitation for valid employee and role")
    void invite_valid_createsPending() {
        Employee emp = activeEmployee();
        when(employeeRepository.findByIdAndCompanyId(EMPLOYEE_ID, TENANT_ID)).thenReturn(Optional.of(emp));
        when(invitationRepository.existsByEmployeeIdAndStatus(EMPLOYEE_ID, TenantEmployeeInvitation.InvitationStatus.PENDING)).thenReturn(false);
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(notificationService.sendEmailWithResult(anyString(), anyString(), anyString())).thenReturn(true);

        TenantInvitationDto.InviteRequest req = new TenantInvitationDto.InviteRequest();
        req.setEmployeeId(EMPLOYEE_ID);
        req.setRoleName(PlatformRoles.EMPLOYEE);

        TenantInvitationDto.Response result = service.invite(req);

        assertThat(result.getStatus()).isEqualTo(TenantEmployeeInvitation.InvitationStatus.PENDING);
        assertThat(result.getRoleName()).isEqualTo(PlatformRoles.EMPLOYEE);
        assertThat(result.isEmailDelivered()).isTrue();
        verify(notificationService).sendEmailWithResult(eq(EMP_EMAIL), anyString(), anyString());
    }

    // ── 2. Duplicate invitation rejected ─────────────────────────────────────

    @Test
    @DisplayName("invite() rejects duplicate pending invitation for same employee")
    void invite_duplicatePending_throws() {
        Employee emp = activeEmployee();
        when(employeeRepository.findByIdAndCompanyId(EMPLOYEE_ID, TENANT_ID)).thenReturn(Optional.of(emp));
        when(invitationRepository.existsByEmployeeIdAndStatus(EMPLOYEE_ID, TenantEmployeeInvitation.InvitationStatus.PENDING)).thenReturn(true);

        TenantInvitationDto.InviteRequest req = new TenantInvitationDto.InviteRequest();
        req.setEmployeeId(EMPLOYEE_ID);
        req.setRoleName(PlatformRoles.EMPLOYEE);

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("pending invitation already exists");
    }

    // ── 3. Resend invalidates old token ───────────────────────────────────────

    @Test
    @DisplayName("resendInvitation() rotates the token hash")
    void resend_rotatesTokenHash() {
        TenantEmployeeInvitation inv = pendingInvitation("oldhash");
        when(invitationRepository.findByIdAndTenantId(inv.getId(), TENANT_ID)).thenReturn(Optional.of(inv));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findByIdAndCompanyId(EMPLOYEE_ID, TENANT_ID)).thenReturn(Optional.of(activeEmployee()));
        when(notificationService.sendEmailWithResult(anyString(), anyString(), anyString())).thenReturn(true);

        service.resendInvitation(inv.getId());

        assertThat(inv.getTokenHash()).isNotEqualTo("oldhash");
        assertThat(inv.getStatus()).isEqualTo(TenantEmployeeInvitation.InvitationStatus.PENDING);
    }

    // ── 4. Revoked invitation rejected on accept ──────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects REVOKED invitation")
    void accept_revokedInvitation_throws() {
        String rawToken = "revokedtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.REVOKED);
        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("revoked");
    }

    // ── 5. Expired invitation rejected ───────────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects expired invitation")
    void accept_expiredInvitation_throws() {
        String rawToken = "expiredtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);
        inv.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("expired");
        assertThat(inv.getStatus()).isEqualTo(TenantEmployeeInvitation.InvitationStatus.EXPIRED);
    }

    // ── 6. Token hash validation ──────────────────────────────────────────────

    @Test
    @DisplayName("hashToken() produces 64-char SHA-256 hex and is deterministic")
    void hashToken_properties() {
        String h1 = TenantInvitationService.hashToken("mytoken");
        String h2 = TenantInvitationService.hashToken("mytoken");
        String h3 = TenantInvitationService.hashToken("othertoken");
        assertThat(h1).hasSize(64).matches("[0-9a-f]+");
        assertThat(h1).isEqualTo(h2);
        assertThat(h1).isNotEqualTo(h3);
    }

    // ── 7. Email delivery failure handling ────────────────────────────────────

    @Test
    @DisplayName("invite() returns emailDelivered=false when SMTP fails, invitation is preserved")
    void invite_emailDeliveryFailure_invitationPreserved() {
        Employee emp = activeEmployee();
        when(employeeRepository.findByIdAndCompanyId(EMPLOYEE_ID, TENANT_ID)).thenReturn(Optional.of(emp));
        when(invitationRepository.existsByEmployeeIdAndStatus(EMPLOYEE_ID, TenantEmployeeInvitation.InvitationStatus.PENDING)).thenReturn(false);
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(anyString(), eq(TENANT_ID))).thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(notificationService.sendEmailWithResult(anyString(), anyString(), anyString())).thenReturn(false);

        TenantInvitationDto.InviteRequest req = new TenantInvitationDto.InviteRequest();
        req.setEmployeeId(EMPLOYEE_ID);
        req.setRoleName(PlatformRoles.EMPLOYEE);

        TenantInvitationDto.Response result = service.invite(req);

        assertThat(result.isEmailDelivered()).isFalse();
        assertThat(result.getStatus()).isEqualTo(TenantEmployeeInvitation.InvitationStatus.PENDING);
        // Invitation was still saved
        verify(invitationRepository).save(any());
    }

    // ── 8. Valid verified email accepted ─────────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() creates ACTIVE membership for valid verified email")
    void accept_validVerifiedEmail_createsActiveMembership() {
        String rawToken = "validtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);

        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(false);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findByIdAndCompanyId(EMPLOYEE_ID, TENANT_ID)).thenReturn(Optional.of(activeEmployee()));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantInvitationDto.Response result = service.acceptInvitation(rawToken);

        assertThat(result.getStatus()).isEqualTo(TenantEmployeeInvitation.InvitationStatus.ACCEPTED);
        assertThat(inv.getKeycloakUserId()).isEqualTo(ACTOR_ID);
        verify(membershipRepository).save(argThat(m ->
                m.getMembershipStatus() == MembershipStatus.ACTIVE &&
                m.getRoleName().equals(PlatformRoles.EMPLOYEE)));
    }

    // ── 9. Unverified email rejected ──────────────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects when email_verified=false")
    void accept_unverifiedEmail_throws() {
        String rawToken = "unverifiedtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("email")).thenReturn(EMP_EMAIL);
        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("Email address must be verified");
    }

    // ── 10. Email mismatch rejected ───────────────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects when JWT email does not match invitation email")
    void accept_emailMismatch_throws() {
        String rawToken = "mismatchtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);
        inv.setEmail("other@company.com");

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("does not match the invitation email");
    }

    // ── 11. Invalid token rejected ────────────────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects unknown token")
    void accept_invalidToken_throws() {
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptInvitation("badtoken"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid or unknown invitation token");
    }

    // ── 12. Already accepted token rejected ───────────────────────────────────

    @Test
    @DisplayName("acceptInvitation() rejects already ACCEPTED invitation")
    void accept_alreadyAccepted_throws() {
        String rawToken = "usedtoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.ACCEPTED);
        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("already been accepted");
    }

    // ── 13. Simultaneous acceptance — duplicate membership rejected ───────────

    @Test
    @DisplayName("acceptInvitation() rejects when membership already exists (concurrent acceptance guard)")
    void accept_duplicateMembership_throws() {
        String rawToken = "concurrenttoken";
        String hash = TenantInvitationService.hashToken(rawToken);
        TenantEmployeeInvitation inv = pendingInvitation(hash);

        when(invitationRepository.findByTokenHash(hash)).thenReturn(Optional.of(inv));
        when(membershipRepository.existsByKeycloakUserIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.acceptInvitation(rawToken))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("membership already exists");
    }

    // ── 14. Tenant mismatch — platform tenant rejected ────────────────────────

    @Test
    @DisplayName("invite() rejects when TenantContext is the platform pseudo-tenant")
    void invite_platformTenant_throws() {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());

        TenantInvitationDto.InviteRequest req = new TenantInvitationDto.InviteRequest();
        req.setEmployeeId(EMPLOYEE_ID);
        req.setRoleName(PlatformRoles.EMPLOYEE);

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("platform pseudo-tenant");
    }

    // ── 15. Platform role escalation rejected ─────────────────────────────────

    @Test
    @DisplayName("invite() rejects platform roles (PLATFORM_ADMIN, etc.)")
    void invite_platformRole_throws() {
        TenantInvitationDto.InviteRequest req = new TenantInvitationDto.InviteRequest();
        req.setEmployeeId(EMPLOYEE_ID);
        req.setRoleName(PlatformRoles.PLATFORM_ADMIN);

        assertThatThrownBy(() -> service.invite(req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid tenant role");
    }

    // ── 16. Revoke only PENDING ───────────────────────────────────────────────

    @Test
    @DisplayName("revokeInvitation() rejects revoking an ACCEPTED invitation")
    void revoke_accepted_throws() {
        TenantEmployeeInvitation inv = pendingInvitation("hash");
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.ACCEPTED);
        when(invitationRepository.findByIdAndTenantId(inv.getId(), TENANT_ID)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.revokeInvitation(inv.getId()))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only PENDING invitations can be revoked");
    }

    // ── 17. Tenant A cannot access Tenant B invitations ───────────────────────

    @Test
    @DisplayName("revokeInvitation() rejects invitation belonging to a different tenant")
    void revoke_wrongTenant_throws() {
        UUID otherTenantId = UUID.randomUUID();
        TenantContext.setTenantId(otherTenantId.toString());

        UUID invId = UUID.randomUUID();
        when(invitationRepository.findByIdAndTenantId(invId, otherTenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeInvitation(invId))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invitation not found");
    }
}
