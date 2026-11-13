package com.platform.hr.platform.employees;

import com.platform.notification.NotificationService;
import com.platform.security.rbac.PlatformRoles;
import com.platform.security.user.KeycloakUserService;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.MembershipValidationService;
import com.platform.tenancy.membership.UserTenantMembership;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Platform employee management service.
 *
 * <p>Platform employees are {@link UserTenantMembership} records in the platform
 * pseudo-tenant ({@code 00000000-0000-0000-0000-000000000001}).
 *
 * <p>Security invariants enforced here:
 * <ul>
 *   <li>Only platform roles from the allowlist can be assigned.</li>
 *   <li>An admin cannot grant a higher role than they currently hold.</li>
 *   <li>The last active PLATFORM_SUPER_ADMIN cannot be suspended, revoked or demoted.</li>
 *   <li>Platform memberships are never created automatically — only via accepted invitation.</li>
 *   <li>Invitation tokens are stored only as SHA-256 hashes — raw tokens are never persisted.</li>
 *   <li>Acceptance verifies the authenticated user's email matches the invitation.</li>
 *   <li>Membership creation on acceptance is transactional and single-use.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformEmployeeService {

    private static final Set<String> ALLOWED_PLATFORM_ROLES = Set.of(
            PlatformRoles.PLATFORM_SUPER_ADMIN,
            PlatformRoles.PLATFORM_ADMIN,
            PlatformRoles.PLATFORM_SUPPORT,
            PlatformRoles.PLATFORM_IMPLEMENTATION
    );

    private static final int INVITATION_EXPIRY_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserTenantMembershipRepository membershipRepository;
    private final MembershipValidationService membershipValidationService;
    private final PlatformInvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakUserService keycloakUserService;
    private final NotificationService notificationService;

    // ── List platform employees ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PlatformEmployeeDto> listEmployees(int page, int size, MembershipStatus status) {
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserTenantMembership> memberships = status != null
                ? membershipRepository.findByTenantIdAndMembershipStatusPageable(platformId, status, pageable)
                : membershipRepository.findByTenantIdPageable(platformId, pageable);

        return memberships.map(PlatformEmployeeDto::from);
    }

    @Transactional(readOnly = true)
    public PlatformEmployeeDto getEmployee(UUID membershipId) {
        UserTenantMembership m = requirePlatformMembership(membershipId);
        return PlatformEmployeeDto.from(m);
    }

    // ── Invite ────────────────────────────────────────────────────────────────

    @Transactional
    public InvitationDto invite(InviteRequest req) {
        validateRole(req.getRoleName());
        preventPrivilegeEscalation(req.getRoleName());

        if (invitationRepository.existsByEmailAndStatus(
                req.getEmail(), PlatformInvitation.InvitationStatus.PENDING)) {
            throw new TenantException("A pending invitation already exists for: " + req.getEmail());
        }

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        PlatformInvitation invitation = PlatformInvitation.builder()
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .roleName(req.getRoleName())
                .tokenHash(tokenHash)
                .invitedBy(currentActorId())
                .expiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS))
                .build();

        invitation = invitationRepository.save(invitation);
        log.info("Platform invitation created: email={} role={} id={}",
                req.getEmail(), req.getRoleName(), invitation.getId());

        sendInvitationEmail(invitation, rawToken);
        // rawToken is discarded after this point — never stored or returned in any response
        return InvitationDto.from(invitation);
    }

    // ── Accept invitation ─────────────────────────────────────────────────────

    /**
     * Accepts a platform invitation.
     *
     * <p>Security checks performed in order:
     * <ol>
     *   <li>Token hash lookup — invalid token returns 400 (no timing oracle).</li>
     *   <li>Status must be PENDING (not REVOKED, ACCEPTED, or EXPIRED).</li>
     *   <li>Invitation must not be past its expiry time.</li>
     *   <li>Authenticated user's email (from JWT {@code email} claim) must match the invitation.</li>
     *   <li>JWT {@code email_verified} claim must be {@code true}.</li>
     *   <li>No existing membership for this user in the platform tenant.</li>
     *   <li>Membership creation and invitation status update are atomic in one transaction.</li>
     * </ol>
     *
     * <p>The role is taken exclusively from the invitation record — no role parameter
     * is accepted from the caller, preventing role injection via browser parameters.
     *
     * @param rawToken the raw token from the invitation link (never stored)
     * @return the created ACTIVE platform membership DTO
     */
    @Transactional
    public PlatformEmployeeDto acceptInvitation(String rawToken) {
        String tokenHash = hashToken(rawToken);

        PlatformInvitation inv = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TenantException("Invalid or unknown invitation token"));

        if (inv.getStatus() == PlatformInvitation.InvitationStatus.REVOKED) {
            throw new TenantException("This invitation has been revoked");
        }
        if (inv.getStatus() == PlatformInvitation.InvitationStatus.ACCEPTED) {
            throw new TenantException("This invitation has already been accepted");
        }
        if (inv.getStatus() == PlatformInvitation.InvitationStatus.EXPIRED || inv.isExpired()) {
            inv.setStatus(PlatformInvitation.InvitationStatus.EXPIRED);
            invitationRepository.save(inv);
            throw new TenantException("This invitation has expired");
        }

        // Verify the authenticated user's email matches the invitation email
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

        String keycloakUserId = jwt.getSubject();
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;

        // Prevent duplicate membership — also guards against concurrent acceptance
        if (membershipRepository.existsByKeycloakUserIdAndTenantId(keycloakUserId, platformId)) {
            throw new TenantException("A platform membership already exists for this user");
        }

        // Role is authoritative from the invitation — never from a request parameter
        var platformTenant = tenantRepository.findById(platformId)
                .orElseThrow(() -> new TenantException("Platform tenant not found"));

        UserTenantMembership membership = UserTenantMembership.builder()
                .keycloakUserId(keycloakUserId)
                .tenant(platformTenant)
                .roleName(inv.getRoleName())
                .membershipStatus(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(membership);

        // Mark invitation accepted — single-use enforced by ACCEPTED status check above
        inv.setStatus(PlatformInvitation.InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        inv.setKeycloakUserId(keycloakUserId);
        invitationRepository.save(inv);

        log.info("Platform invitation accepted: invitationId={} keycloakUserId={} role={}",
                inv.getId(), keycloakUserId, inv.getRoleName());
        return PlatformEmployeeDto.from(membership);
    }

    // ── Resend invitation ─────────────────────────────────────────────────────

    /**
     * Resends an invitation with a rotated token.
     * The previous token hash is replaced — the old link is immediately invalidated.
     */
    @Transactional
    public InvitationDto resendInvitation(UUID invitationId) {
        PlatformInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new TenantException("Invitation not found: " + invitationId));

        if (inv.getStatus() == PlatformInvitation.InvitationStatus.ACCEPTED) {
            throw new TenantException("Cannot resend an accepted invitation");
        }
        if (inv.getStatus() == PlatformInvitation.InvitationStatus.REVOKED) {
            throw new TenantException("Cannot resend a revoked invitation");
        }

        // Rotate token — old hash is overwritten, old link is immediately invalidated
        String rawToken = generateToken();
        inv.setTokenHash(hashToken(rawToken));
        inv.setStatus(PlatformInvitation.InvitationStatus.PENDING);
        inv.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));
        invitationRepository.save(inv);
        log.info("Platform invitation resent with token rotation: id={}", invitationId);

        sendInvitationEmail(inv, rawToken);
        return InvitationDto.from(inv);
    }

    // ── Revoke invitation ─────────────────────────────────────────────────────

    @Transactional
    public InvitationDto revokeInvitation(UUID invitationId) {
        PlatformInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new TenantException("Invitation not found: " + invitationId));

        if (inv.getStatus() != PlatformInvitation.InvitationStatus.PENDING) {
            throw new TenantException("Only PENDING invitations can be revoked");
        }
        inv.setStatus(PlatformInvitation.InvitationStatus.REVOKED);
        invitationRepository.save(inv);
        return InvitationDto.from(inv);
    }

    // ── List invitations ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InvitationDto> listInvitations(int page, int size,
                                                PlatformInvitation.InvitationStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PlatformInvitation> invitations = status != null
                ? invitationRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : invitationRepository.findAll(pageable);
        return invitations.map(InvitationDto::from);
    }

    // ── Change role ───────────────────────────────────────────────────────────

    @Transactional
    public PlatformEmployeeDto changeRole(UUID membershipId, String newRole) {
        validateRole(newRole);
        preventPrivilegeEscalation(newRole);

        UserTenantMembership m = requirePlatformMembership(membershipId);

        if (PlatformRoles.PLATFORM_SUPER_ADMIN.equals(m.getRoleName())
                && !PlatformRoles.PLATFORM_SUPER_ADMIN.equals(newRole)) {
            ensureNotLastSuperAdmin(m.getKeycloakUserId());
        }

        m.setRoleName(newRole);
        membershipRepository.save(m);
        log.info("Platform employee role changed: membership={} newRole={}", membershipId, newRole);
        return PlatformEmployeeDto.from(m);
    }

    // ── Suspend membership ────────────────────────────────────────────────────

    @Transactional
    public PlatformEmployeeDto suspendEmployee(UUID membershipId) {
        UserTenantMembership m = requirePlatformMembership(membershipId);

        if (m.getMembershipStatus() != MembershipStatus.ACTIVE) {
            throw new TenantException("Only ACTIVE memberships can be suspended");
        }
        if (PlatformRoles.PLATFORM_SUPER_ADMIN.equals(m.getRoleName())) {
            ensureNotLastSuperAdmin(m.getKeycloakUserId());
        }

        m.setMembershipStatus(MembershipStatus.SUSPENDED);
        membershipRepository.save(m);
        log.info("Platform employee suspended: membership={}", membershipId);
        return PlatformEmployeeDto.from(m);
    }

    // ── Reactivate membership ─────────────────────────────────────────────────

    @Transactional
    public PlatformEmployeeDto reactivateEmployee(UUID membershipId) {
        UserTenantMembership m = requirePlatformMembership(membershipId);

        if (m.getMembershipStatus() != MembershipStatus.SUSPENDED) {
            throw new TenantException("Only SUSPENDED memberships can be reactivated");
        }

        m.setMembershipStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(m);
        log.info("Platform employee reactivated: membership={}", membershipId);
        return PlatformEmployeeDto.from(m);
    }

    // ── Revoke membership ─────────────────────────────────────────────────────

    @Transactional
    public PlatformEmployeeDto revokeEmployee(UUID membershipId) {
        UserTenantMembership m = requirePlatformMembership(membershipId);

        if (m.getMembershipStatus() == MembershipStatus.REVOKED) {
            throw new TenantException("Membership is already revoked");
        }
        if (PlatformRoles.PLATFORM_SUPER_ADMIN.equals(m.getRoleName())) {
            ensureNotLastSuperAdmin(m.getKeycloakUserId());
        }

        m.setMembershipStatus(MembershipStatus.REVOKED);
        membershipRepository.save(m);
        log.info("Platform employee revoked: membership={}", membershipId);
        return PlatformEmployeeDto.from(m);
    }

    // ── Audit history ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PlatformEmployeeDto> getAllPlatformMemberships() {
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;
        return membershipRepository.findByTenantId(platformId)
                .stream()
                .map(PlatformEmployeeDto::from)
                .toList();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private UserTenantMembership requirePlatformMembership(UUID membershipId) {
        UserTenantMembership m = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new TenantException("Membership not found: " + membershipId));
        if (!TenantSecurityService.PLATFORM_TENANT_ID.equals(m.getTenant().getId())) {
            throw new TenantSecurityException("Membership does not belong to the platform tenant");
        }
        return m;
    }

    private void validateRole(String roleName) {
        if (!ALLOWED_PLATFORM_ROLES.contains(roleName)) {
            throw new TenantException("Invalid platform role: " + roleName +
                    ". Allowed: " + ALLOWED_PLATFORM_ROLES);
        }
    }

    private void preventPrivilegeEscalation(String targetRole) {
        if (PlatformRoles.PLATFORM_SUPER_ADMIN.equals(targetRole)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> PlatformRoles.PLATFORM_SUPER_ADMIN.equals(a.getAuthority()));
            if (!isSuperAdmin) {
                throw new TenantSecurityException(
                        "Only PLATFORM_SUPER_ADMIN can grant PLATFORM_SUPER_ADMIN role");
            }
        }
    }

    private void ensureNotLastSuperAdmin(String excludeKeycloakUserId) {
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;
        long activeSuperAdmins = membershipRepository
                .findByTenantIdAndMembershipStatus(platformId, MembershipStatus.ACTIVE)
                .stream()
                .filter(m -> PlatformRoles.PLATFORM_SUPER_ADMIN.equals(m.getRoleName()))
                .filter(m -> !m.getKeycloakUserId().equals(excludeKeycloakUserId))
                .count();

        if (activeSuperAdmins == 0) {
            throw new TenantException(
                    "Cannot remove or demote the last active PLATFORM_SUPER_ADMIN. " +
                    "Promote another admin first.");
        }
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
     * The raw token is returned to the caller for one-time use in the invitation email.
     * It is NEVER stored — only its SHA-256 hash is persisted.
     */
    private String generateToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the SHA-256 hex digest of the raw token (64 hex chars).
     * This is the only value stored in the database column {@code token_hash}.
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

    /**
     * Sends the invitation email via the existing NotificationService.
     * The raw token is included in the email body for the invitee to use.
     * It is NOT logged at any level.
     *
     * <p>Email delivery is async — a send failure does NOT roll back the invitation record.
     * The admin can resend via the resend endpoint if delivery fails.
     *
     * <p>NOTE: Actual delivery depends on JavaMailSender being configured in the environment
     * (spring.mail.* properties). In local/test profiles without mail config, the send
     * will fail silently (logged as error by NotificationService) but the invitation is saved.
     */
    private void sendInvitationEmail(PlatformInvitation inv, String rawToken) {
        String subject = "You have been invited to WorkSuite Platform";
        String body = String.format(
                "Hello %s %s,%n%n" +
                "You have been invited to join the WorkSuite Platform as: %s%n%n" +
                "Use the following token to accept your invitation:%n%n" +
                "  %s%n%n" +
                "This invitation expires on: %s%n%n" +
                "To accept, POST to: /api/v1/platform/employees/invitations/accept?token=<above>%n%n" +
                "If you did not expect this invitation, please ignore this email.",
                inv.getFirstName(), inv.getLastName(),
                inv.getRoleName(),
                rawToken,
                inv.getExpiresAt().toLocalDate()
        );
        notificationService.sendEmail(inv.getEmail(), subject, body);
    }
}
