package com.platform.hr.platform.tenants;

import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantLifecycleEvent;
import com.platform.tenancy.TenantLifecycleEventRepository;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Platform-level tenant management service.
 *
 * <p>All state-changing operations:
 * <ul>
 *   <li>Validate the transition is legal (forward-only state machine).</li>
 *   <li>Persist the new status on the tenant.</li>
 *   <li>Append an immutable {@link TenantLifecycleEvent} for audit.</li>
 * </ul>
 *
 * <p>The platform pseudo-tenant is excluded from all list/count operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantManagementService {

    private final TenantRepository tenantRepository;
    private final TenantLifecycleEventRepository lifecycleRepository;

    // ── List / Search ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TenantDto> listTenants(int page, int size, String search, Tenant.TenantStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return tenantRepository
                .searchExcluding(TenantSecurityService.PLATFORM_TENANT_ID, status, searchParam, pageable)
                .map(TenantDto::from);
    }

    @Transactional(readOnly = true)
    public TenantDto getById(UUID id) {
        return TenantDto.from(requireTenant(id));
    }

    // ── Registration ──────────────────────────────────────────────────────────

    @Transactional
    public TenantDto register(TenantRegistrationRequest req) {
        if (tenantRepository.existsBySlug(req.getSlug())) {
            throw new TenantException("Slug already taken: " + req.getSlug());
        }
        if (tenantRepository.existsByEmail(req.getEmail())) {
            throw new TenantException("Email already registered: " + req.getEmail());
        }
        Tenant tenant = Tenant.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .email(req.getEmail())
                .phone(req.getPhone())
                .status(Tenant.TenantStatus.PENDING_REVIEW)
                .isActive(true)
                .build();
        tenant = tenantRepository.save(tenant);
        log.info("Tenant registered: id={} slug={}", tenant.getId(), tenant.getSlug());
        return TenantDto.from(tenant);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Transactional
    public TenantDto approve(UUID id, String reason) {
        Tenant tenant = requireTenant(id);
        transition(tenant, Tenant.TenantStatus.ACTIVE, reason);
        tenant.setReviewedBy(currentActorId());
        tenant.setReviewedAt(LocalDateTime.now());
        tenant.setReviewNotes(reason);
        tenantRepository.save(tenant);
        log.info("Tenant approved: id={}", id);
        return TenantDto.from(tenant);
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Transactional
    public TenantDto reject(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new TenantException("Rejection reason is required");
        }
        Tenant tenant = requireTenant(id);
        transition(tenant, Tenant.TenantStatus.REJECTED, reason);
        tenant.setIsActive(false);
        tenant.setReviewedBy(currentActorId());
        tenant.setReviewedAt(LocalDateTime.now());
        tenant.setReviewNotes(reason);
        tenantRepository.save(tenant);
        log.info("Tenant rejected: id={}", id);
        return TenantDto.from(tenant);
    }

    // ── Suspend ───────────────────────────────────────────────────────────────

    @Transactional
    public TenantDto suspend(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new TenantException("Suspension reason is required");
        }
        Tenant tenant = requireTenant(id);
        transition(tenant, Tenant.TenantStatus.SUSPENDED, reason);
        tenantRepository.save(tenant);
        log.info("Tenant suspended: id={}", id);
        return TenantDto.from(tenant);
    }

    // ── Reactivate ────────────────────────────────────────────────────────────

    @Transactional
    public TenantDto reactivate(UUID id, String reason) {
        Tenant tenant = requireTenant(id);
        transition(tenant, Tenant.TenantStatus.ACTIVE, reason);
        tenantRepository.save(tenant);
        log.info("Tenant reactivated: id={}", id);
        return TenantDto.from(tenant);
    }

    // ── Lifecycle history ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TenantLifecycleEventDto> getHistory(UUID id) {
        requireTenant(id); // ensure tenant exists
        return lifecycleRepository.findByTenantIdOrderByOccurredAtDesc(id)
                .stream()
                .map(TenantLifecycleEventDto::from)
                .toList();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Tenant requireTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantException("Tenant not found: " + id));
        if (TenantSecurityService.PLATFORM_TENANT_ID.equals(id)) {
            throw new TenantException("Platform pseudo-tenant cannot be managed via this API");
        }
        return tenant;
    }

    private void transition(Tenant tenant, Tenant.TenantStatus next, String reason) {
        Tenant.TenantStatus current = tenant.getStatus();
        if (!current.canTransitionTo(next)) {
            throw new TenantException(
                    "Invalid status transition: " + current + " → " + next);
        }
        tenant.setStatus(next);
        appendEvent(tenant.getId(), current, next, reason);
    }

    private void appendEvent(UUID tenantId, Tenant.TenantStatus from,
                             Tenant.TenantStatus to, String reason) {
        TenantLifecycleEvent event = TenantLifecycleEvent.builder()
                .tenantId(tenantId)
                .actorId(currentActorId())
                .actorRole(currentActorRole())
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .build();
        lifecycleRepository.save(event);
    }

    private String currentActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return "system";
    }

    private String currentActorRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "UNKNOWN";
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(r -> r.startsWith("PLATFORM_"))
                .findFirst()
                .orElse("UNKNOWN");
    }
}
