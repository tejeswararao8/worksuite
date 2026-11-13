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
 *   <li>An admin cannot grant themselves a higher role than they currently hold.</li>
 *   <li>The last active PLATFORM_SUPER_ADMIN cannot be suspended or revoked.</li>
 *   <li>Platform memberships are never created automatically.</li>
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
        // rawToken is discarded after this point — never stored or returned
        return InvitationDto.from(invitation);
    }

    // ── Accept invitation ─────────────────────────────────────────────────────

    /**
     * Accepts a platform invitation.
     *
     * <p>Security checks performed in order:
     * <ol>
     *   <li>Token hash lookup — invalid token returns 400 (no oracle).</li>
     *   <li>Status must be PENDING.</li>
     *   <li>Invitation must not be expired.</li>
     *   <li>Authenticated user's email (from JWT) must match the invitation email.</li>
     *   <li>No existing ACTIVE membership for this user in the platform tenant.</li>
     *   <li>Membership creation and invitation status update are atomic.</li>
     * </ol>
     *
     * @param rawToken the raw token from the invitation link (never stored)
     * @return the created platform membership
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

        // Verify the authenticated user's email matches the invitation
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

        // Prevent duplicate ACTIVE membership
        if (membershipRepository.existsByKeycloakUserIdAndTenantId(keycloakUserId, platformId)) {
            throw new TenantException(
                    "A platform membership already exists for this user");
        }

        // Create ACTIVE membership — role is taken from the invitation, not from any request param
        var platformTenant = tenantRepository.findById(platformId)
                .orElseThrow(() -> new TenantException("Platform tenant not found"));

        UserTenantMembership membership = UserTenantMembership.builder()
                .keycloakUserId(keycloakUserId)
                .tenant(platformTenant)
                .roleName(inv.getRoleName())   // role is authoritative from invitation
                .membershipStatus(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(membership);

        // Mark invitation accepted — single-use enforced
        inv.setStatus(PlatformInvitation.InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        inv.setKeycloakUserId(keycloakUserId);
        invitationRepository.save(inv);

        log.info("Platform invitation accepted: invitationId={} keycloakUserId={} role={}",
                inv.getId(), keycloakUserId, inv.getRoleName());
        return PlatformEmployeeDto.from(membership);
    }

    // ── Resend invitation ─────────────────────────────────────────────────────

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

        // Rotate token — old hash is replaced, old link is immediately invalidated
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

        // Prevent removing the last SUPER_ADMIN by role change
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
        // Only PLATFORM_SUPER_ADMIN can grant PLATFORM_SUPER_ADMIN
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
     * Generates a cryptographically secure random token (48 bytes = 64 Base64URL chars).
     * The raw token is returned to the caller for one-time use (e.g. email link).
     * It is NEVER stored — only its SHA-256 hash is persisted.
     */
    private String generateToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the SHA-256 hex digest of the raw token.
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

    private void sendInvitationEmail(PlatformInvitation inv, String rawToken) {
        String subject = "You have been invited to WorkSuite Platform";
        String body = String.format(
                "Hello %s %s,%n%n" +
                "You have been invited to join the WorkSuite Platform as %s.%n%n" +
                "To accept this invitation, use the following token:%n%n" +
                "  %s%n%n" +
                "This invitation expires on: %s%n%n" +
                "If you did not expect this invitation, please ignore this email.",
                inv.getFirstName(), inv.getLastName(),
                inv.getRoleName(),
                rawToken,
                inv.getExpiresAt().toLocalDate()
        );
        // Async — failure does not roll back the invitation record
        notificationService.sendEmail(inv.getEmail(), subject, body);
    }
}


/**
 * Platform employee management service.
 *
 * <p>Platform employees are {@link UserTenantMembership} records in the platform
 * pseudo-tenant ({@code 00000000-0000-0000-0000-000000000001}).
 *
 * <p>Security invariants enforced here:
 * <ul>
 *   <li>Only platform roles from the allowlist can be assigned.</li>
 *   <li>An admin cannot grant themselves a higher role than they currently hold.</li>
 *   <li>The last active PLATFORM_SUPER_ADMIN cannot be suspended or revoked.</li>
 *   <li>Platform memberships are never created automatically.</li>
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

        String token = generateToken();
        PlatformInvitation invitation = PlatformInvitation.builder()
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .roleName(req.getRoleName())
                .token(token)
                .invitedBy(currentActorId())
                .expiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS))
                .build();

        invitation = invitationRepository.save(invitation);
        log.info("Platform invitation created: email={} role={}", req.getEmail(), req.getRoleName());
        return InvitationDto.from(invitation);
    }

    // ── Resend invitation ─────────────────────────────────────────────────────

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

        inv.setToken(generateToken());
        inv.setStatus(PlatformInvitation.InvitationStatus.PENDING);
        inv.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));
        invitationRepository.save(inv);
        log.info("Platform invitation resent: id={}", invitationId);
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

        // Prevent removing the last SUPER_ADMIN by role change
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
        // Only PLATFORM_SUPER_ADMIN can grant PLATFORM_SUPER_ADMIN
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

    private String generateToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
