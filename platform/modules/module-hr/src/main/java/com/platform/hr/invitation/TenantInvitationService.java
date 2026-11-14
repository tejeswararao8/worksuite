package com.platform.hr.invitation;

import com.platform.api.PlatformException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

/**
 * Tenant employee invitation service.
 *
 * <p>Security invariants enforced here:
 * <ul>
 *   <li>Tenant ID always comes from TenantContext (JWT-derived) — never from a client parameter.</li>
 *   <li>Platform pseudo-tenant cannot use this service.</li>
 *   <li>Only allowed tenant roles can be assigned via invitation.</li>
 *   <li>Invitation tokens are stored only as SHA-256 hashes — raw tokens are never persisted.</li>
 *   <li>Acceptance verifies the authenticated user's email matches the invitation.</li>
 *   <li>email_verified=true is required before membership activation.</li>
 *   <li>Membership creation and invitation status update are atomic in one transaction.</li>
 *   <li>Pessimistic lock on token hash lookup prevents concurrent double-acceptance.</li>
 *   <li>Platform roles cannot be granted through this service.</li>
 *   <li>Employee must belong to the requesting tenant.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInvitationService {

    private static final Set<String> ALLOWED_TENANT_ROLES = Set.of(
            PlatformRoles.TENANT_OWNER,
            PlatformRoles.TENANT_ADMIN,
            PlatformRoles.HR_ADMIN,
            PlatformRoles.HR_MANAGER,
            PlatformRoles.EMPLOYEE
    );

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${platform.invitation.expiry-days:7}")
    private int invitationExpiryDays;

    private final TenantEmployeeInvitationRepository invitationRepository;
    private final EmployeeRepository employeeRepository;
    private final UserTenantMembershipRepository membershipRepository;
    private final TenantRepository tenantRepository;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    // ── Invite ────────────────────────────────────────────────────────────────

    @Transactional
    public TenantInvitationDto.Response invite(TenantInvitationDto.InviteRequest req) {
        UUID tenantId = currentTenantId();
        rejectPlatformTenant(tenantId);
        validateRole(req.getRoleName());

        Employee employee = requireEmployeeForTenant(req.getEmployeeId(), tenantId);

        if (invitationRepository.existsByEmployeeIdAndStatus(
                employee.getId(), TenantEmployeeInvitation.InvitationStatus.PENDING)) {
            throw new TenantException(
                    "A pending invitation already exists for employee: " + employee.getEmail());
        }

        // Check if employee already has an active membership
        if (membershipRepository.existsByKeycloakUserIdAndTenantId(
                employee.getKeycloakUserId() != null ? employee.getKeycloakUserId() : "",
                tenantId)) {
            throw new TenantException("Employee already has an active membership in this tenant.");
        }

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        TenantEmployeeInvitation invitation = TenantEmployeeInvitation.builder()
                .tenantId(tenantId)
                .employeeId(employee.getId())
                .email(employee.getEmail())
                .roleName(req.getRoleName())
                .tokenHash(tokenHash)
                .invitedBy(currentActorId())
                .expiresAt(LocalDateTime.now().plusDays(invitationExpiryDays))
                .build();

        invitation = invitationRepository.save(invitation);

        // Mark employee as pending invitation
        employee.setEmploymentStatus(EmploymentStatus.PENDING_INVITATION);
        employeeRepository.save(employee);

        log.info("Tenant invitation created: tenantId={} employeeId={} email={} invitationId={}",
                tenantId, employee.getId(), employee.getEmail(), invitation.getId());

        boolean emailSent = sendInvitationEmail(invitation, rawToken, employee);
        // rawToken discarded after this point — never stored or returned
        if (!emailSent) {
            log.warn("Invitation created but email delivery failed: id={} email={}",
                    invitation.getId(), invitation.getEmail());
            return TenantInvitationDto.Response.fromWithDeliveryFailure(invitation);
        }
        return TenantInvitationDto.Response.from(invitation);
    }

    // ── Accept invitation ─────────────────────────────────────────────────────

    /**
     * Accepts a tenant employee invitation.
     *
     * <p>Security checks in order:
     * <ol>
     *   <li>Token hash lookup — invalid token returns 400.</li>
     *   <li>Status must be PENDING.</li>
     *   <li>Invitation must not be past expiry.</li>
     *   <li>JWT email must match invitation email.</li>
     *   <li>email_verified must be true.</li>
     *   <li>No existing membership for this user in this tenant.</li>
     *   <li>Membership creation and invitation update are atomic.</li>
     *   <li>Role is taken from the invitation — never from the request.</li>
     *   <li>Platform roles cannot be granted.</li>
     * </ol>
     */
    @Transactional
    public TenantInvitationDto.Response acceptInvitation(String rawToken) {
        String tokenHash = hashToken(rawToken);

        TenantEmployeeInvitation inv = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TenantException("Invalid or unknown invitation token"));

        if (inv.getStatus() == TenantEmployeeInvitation.InvitationStatus.REVOKED) {
            throw new TenantException("This invitation has been revoked");
        }
        if (inv.getStatus() == TenantEmployeeInvitation.InvitationStatus.ACCEPTED) {
            throw new TenantException("This invitation has already been accepted");
        }
        if (inv.getStatus() == TenantEmployeeInvitation.InvitationStatus.EXPIRED || inv.isExpired()) {
            inv.setStatus(TenantEmployeeInvitation.InvitationStatus.EXPIRED);
            invitationRepository.save(inv);
            throw new TenantException("This invitation has expired");
        }

        // Verify authenticated user's email matches invitation
        Jwt jwt = currentJwt();
        String jwtEmail = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        if (jwtEmail == null || !jwtEmail.equalsIgnoreCase(inv.getEmail())) {
            throw new TenantSecurityException(
                    "Authenticated user email does not match the invitation email");
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new TenantSecurityException(
                    "Email address must be verified before accepting an invitation");
        }

        // Guard: role must not be a platform role
        if (!ALLOWED_TENANT_ROLES.contains(inv.getRoleName())) {
            throw new TenantSecurityException(
                    "Invitation contains an invalid role: " + inv.getRoleName());
        }

        String keycloakUserId = jwt.getSubject();
        UUID tenantId = inv.getTenantId();

        // Prevent cross-tenant membership creation
        rejectPlatformTenant(tenantId);

        // Prevent duplicate membership
        if (membershipRepository.existsByKeycloakUserIdAndTenantId(keycloakUserId, tenantId)) {
            throw new TenantException("A membership already exists for this user in this tenant");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException("Tenant not found: " + tenantId));

        // Role is authoritative from the invitation — never from a request parameter
        UserTenantMembership membership = UserTenantMembership.builder()
                .keycloakUserId(keycloakUserId)
                .tenant(tenant)
                .roleName(inv.getRoleName())
                .membershipStatus(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(membership);

        // Link Keycloak identity to employee record and activate
        Employee employee = employeeRepository.findByIdAndCompanyId(inv.getEmployeeId(), tenantId)
                .orElseThrow(() -> new TenantException("Employee record not found"));
        employee.setKeycloakUserId(keycloakUserId);
        if (employee.getEmploymentStatus() == EmploymentStatus.PENDING_INVITATION) {
            employee.setEmploymentStatus(EmploymentStatus.PROBATION);
        }
        employeeRepository.save(employee);

        // Mark invitation accepted — single-use enforced by ACCEPTED status check above
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        inv.setKeycloakUserId(keycloakUserId);
        invitationRepository.save(inv);

        log.info("Tenant invitation accepted: invitationId={} keycloakUserId={} tenantId={} role={}",
                inv.getId(), keycloakUserId, tenantId, inv.getRoleName());

        return TenantInvitationDto.Response.from(inv);
    }

    // ── Resend ────────────────────────────────────────────────────────────────

    @Transactional
    public TenantInvitationDto.Response resendInvitation(UUID invitationId) {
        UUID tenantId = currentTenantId();
        TenantEmployeeInvitation inv = requireInvitationForTenant(invitationId, tenantId);

        if (inv.getStatus() == TenantEmployeeInvitation.InvitationStatus.ACCEPTED) {
            throw new TenantException("Cannot resend an accepted invitation");
        }
        if (inv.getStatus() == TenantEmployeeInvitation.InvitationStatus.REVOKED) {
            throw new TenantException("Cannot resend a revoked invitation");
        }

        // Rotate token — old hash is overwritten, old link is immediately invalidated
        String rawToken = generateToken();
        inv.setTokenHash(hashToken(rawToken));
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.PENDING);
        inv.setExpiresAt(LocalDateTime.now().plusDays(invitationExpiryDays));
        invitationRepository.save(inv);

        log.info("Tenant invitation resent with token rotation: id={}", invitationId);

        Employee employee = employeeRepository.findByIdAndCompanyId(inv.getEmployeeId(), tenantId)
                .orElse(null);
        boolean emailSent = sendInvitationEmail(inv, rawToken, employee);
        if (!emailSent) {
            log.warn("Invitation resent but email delivery failed: id={} email={}", invitationId, inv.getEmail());
            return TenantInvitationDto.Response.fromWithDeliveryFailure(inv);
        }
        return TenantInvitationDto.Response.from(inv);
    }

    // ── Revoke ────────────────────────────────────────────────────────────────

    @Transactional
    public TenantInvitationDto.Response revokeInvitation(UUID invitationId) {
        UUID tenantId = currentTenantId();
        TenantEmployeeInvitation inv = requireInvitationForTenant(invitationId, tenantId);

        if (inv.getStatus() != TenantEmployeeInvitation.InvitationStatus.PENDING) {
            throw new TenantException("Only PENDING invitations can be revoked");
        }
        inv.setStatus(TenantEmployeeInvitation.InvitationStatus.REVOKED);
        invitationRepository.save(inv);

        // Revert employee status if still pending
        employeeRepository.findByIdAndCompanyId(inv.getEmployeeId(), tenantId).ifPresent(emp -> {
            if (emp.getEmploymentStatus() == EmploymentStatus.PENDING_INVITATION) {
                emp.setEmploymentStatus(EmploymentStatus.PROBATION);
                employeeRepository.save(emp);
            }
        });

        log.info("Tenant invitation revoked: id={} tenantId={}", invitationId, tenantId);
        return TenantInvitationDto.Response.from(inv);
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TenantInvitationDto.Response> listInvitations(
            int page, int size, TenantEmployeeInvitation.InvitationStatus status) {
        UUID tenantId = currentTenantId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TenantEmployeeInvitation> invitations = status != null
                ? invitationRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                : invitationRepository.findByTenantId(tenantId, pageable);
        return invitations.map(TenantInvitationDto.Response::from);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Employee requireEmployeeForTenant(UUID employeeId, UUID tenantId) {
        return employeeRepository.findByIdAndCompanyId(employeeId, tenantId)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private TenantEmployeeInvitation requireInvitationForTenant(UUID invitationId, UUID tenantId) {
        return invitationRepository.findByIdAndTenantId(invitationId, tenantId)
                .orElseThrow(() -> new TenantException("Invitation not found: " + invitationId));
    }

    private void validateRole(String roleName) {
        if (!ALLOWED_TENANT_ROLES.contains(roleName)) {
            throw new TenantException("Invalid tenant role: " + roleName +
                    ". Allowed: " + ALLOWED_TENANT_ROLES);
        }
    }

    private void rejectPlatformTenant(UUID tenantId) {
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            throw new TenantSecurityException(
                    "Tenant employee invitations cannot be used for the platform pseudo-tenant");
        }
    }

    private UUID currentTenantId() {
        String raw = TenantContext.getTenantId();
        if (raw == null || raw.isBlank()) {
            throw new TenantSecurityException("No tenant context available");
        }
        return UUID.fromString(raw);
    }

    private String currentActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return "system";
    }

    private Jwt currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new TenantSecurityException("No authenticated JWT principal found");
    }

    /**
     * Generates a cryptographically secure random token (48 bytes → 64 Base64URL chars).
     * The raw token is returned for one-time use in the invitation email.
     * It is NEVER stored — only its SHA-256 hash is persisted.
     */
    private String generateToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the SHA-256 hex digest of the raw token (64 hex chars).
     * This is the only value stored in the database.
     */
    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean sendInvitationEmail(TenantEmployeeInvitation inv, String rawToken, Employee employee) {
        String firstName = employee != null ? employee.getFirstName() : "Employee";
        String lastName  = employee != null ? employee.getLastName()  : "";
        String subject = "You have been invited to join WorkSuite HRMS";
        String body = String.format(
                "Hello %s %s,%n%n" +
                "You have been invited to join your organization on WorkSuite HRMS as: %s%n%n" +
                "To accept your invitation, submit the following token in the POST body:%n%n" +
                "  POST /api/v1/hr/invitations/accept%n" +
                "  Content-Type: application/json%n" +
                "  { \"token\": \"<your-token>\" }%n%n" +
                "Your token (copy exactly):%n" +
                "  %s%n%n" +
                "This invitation expires on: %s%n%n" +
                "If you did not expect this invitation, please ignore this email.",
                firstName, lastName,
                inv.getRoleName(),
                rawToken,
                inv.getExpiresAt().toLocalDate()
        );
        return notificationService.sendEmailWithResult(inv.getEmail(), subject, body);
    }
}
