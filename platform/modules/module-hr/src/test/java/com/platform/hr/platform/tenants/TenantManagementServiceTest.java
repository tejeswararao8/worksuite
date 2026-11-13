package com.platform.hr.platform.tenants;

import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantLifecycleEvent;
import com.platform.tenancy.TenantLifecycleEventRepository;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
@DisplayName("TenantManagementService")
class TenantManagementServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private TenantLifecycleEventRepository lifecycleRepository;

    @InjectMocks
    private TenantManagementService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String ACTOR_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setupSecurityContext() {
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR_ID);

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);
        lenient().when(auth.getAuthorities()).thenReturn(Collections.emptyList());

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private Tenant tenantWithStatus(Tenant.TenantStatus status) {
        Tenant t = new Tenant();
        t.setId(TENANT_ID);
        t.setName("Acme Corp");
        t.setSlug("acme");
        t.setEmail("acme@example.com");
        t.setStatus(status);
        t.setIsActive(true);
        return t;
    }

    // ── 1. New tenant is created as PENDING_REVIEW ────────────────────────────

    @Test
    @DisplayName("register() creates tenant in PENDING_REVIEW status")
    void register_createsPendingReview() {
        TenantRegistrationRequest req = new TenantRegistrationRequest();
        req.setName("Acme Corp");
        req.setSlug("acme");
        req.setEmail("acme@example.com");

        when(tenantRepository.existsBySlug("acme")).thenReturn(false);
        when(tenantRepository.existsByEmail("acme@example.com")).thenReturn(false);

        Tenant saved = tenantWithStatus(Tenant.TenantStatus.PENDING_REVIEW);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(saved);

        TenantDto result = service.register(req);

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.PENDING_REVIEW);
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Tenant.TenantStatus.PENDING_REVIEW);
    }

    // ── 2. Duplicate slug is rejected ────────────────────────────────────────

    @Test
    @DisplayName("register() rejects duplicate slug")
    void register_duplicateSlug_rejected() {
        TenantRegistrationRequest req = new TenantRegistrationRequest();
        req.setName("Acme Corp");
        req.setSlug("acme");
        req.setEmail("acme@example.com");

        when(tenantRepository.existsBySlug("acme")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Slug already taken");
    }

    // ── 3. Approve PENDING_REVIEW → ACTIVE ───────────────────────────────────

    @Test
    @DisplayName("approve() transitions PENDING_REVIEW to ACTIVE and records event")
    void approve_pendingToActive() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.PENDING_REVIEW);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lifecycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantDto result = service.approve(TENANT_ID, "Looks good");

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        ArgumentCaptor<TenantLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(TenantLifecycleEvent.class);
        verify(lifecycleRepository).save(eventCaptor.capture());
        TenantLifecycleEvent event = eventCaptor.getValue();
        assertThat(event.getFromStatus()).isEqualTo(Tenant.TenantStatus.PENDING_REVIEW);
        assertThat(event.getToStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        assertThat(event.getActorId()).isEqualTo(ACTOR_ID);
    }

    // ── 4. Reject PENDING_REVIEW → REJECTED ──────────────────────────────────

    @Test
    @DisplayName("reject() transitions PENDING_REVIEW to REJECTED and records event")
    void reject_pendingToRejected() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.PENDING_REVIEW);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lifecycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantDto result = service.reject(TENANT_ID, "Incomplete information");

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.REJECTED);
        ArgumentCaptor<TenantLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(TenantLifecycleEvent.class);
        verify(lifecycleRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(Tenant.TenantStatus.REJECTED);
        assertThat(eventCaptor.getValue().getReason()).isEqualTo("Incomplete information");
    }

    // ── 5. Reject without reason is denied ───────────────────────────────────

    @Test
    @DisplayName("reject() without reason throws TenantException")
    void reject_noReason_throws() {
        assertThatThrownBy(() -> service.reject(TENANT_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("reason is required");
        verify(tenantRepository, never()).findById(any());
    }

    // ── 6. Suspend ACTIVE → SUSPENDED ────────────────────────────────────────

    @Test
    @DisplayName("suspend() transitions ACTIVE to SUSPENDED and records event")
    void suspend_activeToSuspended() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.ACTIVE);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lifecycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantDto result = service.suspend(TENANT_ID, "Policy violation");

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.SUSPENDED);
        ArgumentCaptor<TenantLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(TenantLifecycleEvent.class);
        verify(lifecycleRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(Tenant.TenantStatus.SUSPENDED);
    }

    // ── 7. Reactivate SUSPENDED → ACTIVE ─────────────────────────────────────

    @Test
    @DisplayName("reactivate() transitions SUSPENDED to ACTIVE and records event")
    void reactivate_suspendedToActive() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.SUSPENDED);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lifecycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantDto result = service.reactivate(TENANT_ID, "Issue resolved");

        assertThat(result.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        ArgumentCaptor<TenantLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(TenantLifecycleEvent.class);
        verify(lifecycleRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo(Tenant.TenantStatus.SUSPENDED);
        assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
    }

    // ── 8. Invalid transition: ACTIVE → REJECTED ─────────────────────────────

    @Test
    @DisplayName("Invalid transition ACTIVE → REJECTED throws TenantException")
    void invalidTransition_activeToRejected_throws() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.ACTIVE);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.reject(TENANT_ID, "some reason"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid status transition");
        verify(lifecycleRepository, never()).save(any());
    }

    // ── 9. Invalid transition: REJECTED → ACTIVE ─────────────────────────────

    @Test
    @DisplayName("Invalid transition REJECTED → ACTIVE throws TenantException")
    void invalidTransition_rejectedToActive_throws() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.REJECTED);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.approve(TENANT_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid status transition");
    }

    // ── 10. Invalid transition: SUSPENDED → REJECTED ─────────────────────────

    @Test
    @DisplayName("Invalid transition SUSPENDED → REJECTED throws TenantException")
    void invalidTransition_suspendedToRejected_throws() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.SUSPENDED);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.reject(TENANT_ID, "reason"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid status transition");
    }

    // ── 11. Platform pseudo-tenant cannot be managed ──────────────────────────

    @Test
    @DisplayName("Platform pseudo-tenant is rejected from management API")
    void platformTenant_cannotBeManaged() {
        UUID platformId = TenantSecurityService.PLATFORM_TENANT_ID;
        Tenant platform = tenantWithStatus(Tenant.TenantStatus.ACTIVE);
        platform.setId(platformId);
        when(tenantRepository.findById(platformId)).thenReturn(Optional.of(platform));

        assertThatThrownBy(() -> service.getById(platformId))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("pseudo-tenant");
    }

    // ── 12. Tenant not found ──────────────────────────────────────────────────

    @Test
    @DisplayName("getById() throws TenantException when tenant not found")
    void getById_notFound_throws() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("not found");
    }

    // ── 13. Audit history is returned in descending order ─────────────────────

    @Test
    @DisplayName("getHistory() returns events for the tenant")
    void getHistory_returnsEvents() {
        Tenant tenant = tenantWithStatus(Tenant.TenantStatus.ACTIVE);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        TenantLifecycleEvent e1 = TenantLifecycleEvent.builder()
                .id(UUID.randomUUID()).tenantId(TENANT_ID)
                .actorId(ACTOR_ID).actorRole("PLATFORM_ADMIN")
                .fromStatus(Tenant.TenantStatus.PENDING_REVIEW)
                .toStatus(Tenant.TenantStatus.ACTIVE)
                .occurredAt(LocalDateTime.now())
                .build();

        when(lifecycleRepository.findByTenantIdOrderByOccurredAtDesc(TENANT_ID))
                .thenReturn(List.of(e1));

        List<TenantLifecycleEventDto> history = service.getHistory(TENANT_ID);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromStatus()).isEqualTo(Tenant.TenantStatus.PENDING_REVIEW);
        assertThat(history.get(0).getToStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
    }

    // ── 14. Suspend without reason is denied ─────────────────────────────────

    @Test
    @DisplayName("suspend() without reason throws TenantException")
    void suspend_noReason_throws() {
        assertThatThrownBy(() -> service.suspend(TENANT_ID, ""))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("reason is required");
        verify(tenantRepository, never()).findById(any());
    }

    // ── 15. List tenants excludes platform pseudo-tenant ─────────────────────

    @Test
    @DisplayName("listTenants() excludes platform pseudo-tenant")
    void listTenants_excludesPlatformTenant() {
        Page<Tenant> emptyPage = new PageImpl<>(List.of());
        when(tenantRepository.searchExcluding(
                eq(TenantSecurityService.PLATFORM_TENANT_ID),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        Page<TenantDto> result = service.listTenants(0, 20, null, null);

        assertThat(result.getContent()).isEmpty();
        verify(tenantRepository).searchExcluding(
                eq(TenantSecurityService.PLATFORM_TENANT_ID),
                isNull(), isNull(), any(Pageable.class));
    }
}
