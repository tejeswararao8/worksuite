package com.platform.hr.platform.subscription;

import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.Tenant;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityService;
import com.platform.tenancy.membership.MembershipStatus;
import com.platform.tenancy.membership.UserTenantMembershipRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService — Unit Tests")
class SubscriptionServiceTest {

    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private TenantSubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionHistoryRepository historyRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UserTenantMembershipRepository membershipRepository;

    @InjectMocks
    private SubscriptionService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID PLAN_ID   = UUID.randomUUID();
    private static final UUID SUB_ID    = UUID.randomUUID();
    private static final String ACTOR   = UUID.randomUUID().toString();

    @BeforeEach
    void setupAuth() {
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn(ACTOR);
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);
        lenient().when(auth.getAuthorities()).thenAnswer(i ->
                List.of(new SimpleGrantedAuthority(PlatformRoles.PLATFORM_ADMIN)));
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private Tenant tenant() {
        Tenant t = new Tenant();
        t.setId(TENANT_ID);
        t.setName("Acme Corp");
        t.setStatus(Tenant.TenantStatus.ACTIVE);
        t.setIsActive(true);
        return t;
    }

    private SubscriptionPlan activePlan() {
        return SubscriptionPlan.builder()
                .id(PLAN_ID)
                .name("Growth")
                .monthlyPriceCents(7900)
                .annualPriceCents(79000)
                .trialDays(14)
                .maxEmployees(100)
                .storageGb(20)
                .enabledModules("HR,PAYROLL")
                .planStatus(SubscriptionPlan.PlanStatus.ACTIVE)
                .build();
    }

    private TenantSubscription activeSub(TenantSubscription.SubscriptionStatus status) {
        return TenantSubscription.builder()
                .id(SUB_ID)
                .tenant(tenant())
                .plan(activePlan())
                .status(status)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now().minusDays(10))
                .currentPeriodStart(LocalDateTime.now().minusDays(10))
                .currentPeriodEnd(LocalDateTime.now().plusDays(20))
                .assignedBy(ACTOR)
                .build();
    }

    // ── 1. Assign subscription — no existing sub ──────────────────────────────

    @Test
    @DisplayName("assignSubscription() creates ACTIVE subscription when no existing sub")
    void assign_noExisting_createsActive() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));
        when(planRepository.findById(PLAN_ID)).thenReturn(Optional.of(activePlan()));
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AssignSubscriptionRequest req = new AssignSubscriptionRequest();
        req.setPlanId(PLAN_ID);
        req.setStartTrial(false);

        TenantSubscriptionDto result = service.assignSubscription(TENANT_ID, req);

        assertThat(result.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.ACTIVE);
        assertThat(result.getPlan().getId()).isEqualTo(PLAN_ID);
    }

    // ── 2. Assign subscription — starts trial ─────────────────────────────────

    @Test
    @DisplayName("assignSubscription() creates TRIAL subscription when startTrial=true")
    void assign_startTrial_createsTrial() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));
        when(planRepository.findById(PLAN_ID)).thenReturn(Optional.of(activePlan()));
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AssignSubscriptionRequest req = new AssignSubscriptionRequest();
        req.setPlanId(PLAN_ID);
        req.setStartTrial(true);

        TenantSubscriptionDto result = service.assignSubscription(TENANT_ID, req);

        assertThat(result.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.TRIAL);
        assertThat(result.getTrialEndsAt()).isNotNull();
    }

    // ── 3. Assign cancels existing subscription ───────────────────────────────

    @Test
    @DisplayName("assignSubscription() cancels existing non-terminal subscription")
    void assign_cancelsExisting() {
        TenantSubscription existing = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));
        when(planRepository.findById(PLAN_ID)).thenReturn(Optional.of(activePlan()));
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AssignSubscriptionRequest req = new AssignSubscriptionRequest();
        req.setPlanId(PLAN_ID);

        service.assignSubscription(TENANT_ID, req);

        assertThat(existing.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.CANCELLED);
    }

    // ── 4. Platform tenant cannot have subscription ───────────────────────────

    @Test
    @DisplayName("assignSubscription() rejects platform pseudo-tenant")
    void assign_platformTenant_throws() {
        AssignSubscriptionRequest req = new AssignSubscriptionRequest();
        req.setPlanId(PLAN_ID);

        assertThatThrownBy(() ->
                service.assignSubscription(TenantSecurityService.PLATFORM_TENANT_ID, req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Platform pseudo-tenant");
    }

    // ── 5. Inactive plan cannot be assigned ──────────────────────────────────

    @Test
    @DisplayName("assignSubscription() rejects inactive plan")
    void assign_inactivePlan_throws() {
        SubscriptionPlan inactive = activePlan();
        inactive.setPlanStatus(SubscriptionPlan.PlanStatus.INACTIVE);

        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));
        when(planRepository.findById(PLAN_ID)).thenReturn(Optional.of(inactive));

        AssignSubscriptionRequest req = new AssignSubscriptionRequest();
        req.setPlanId(PLAN_ID);

        assertThatThrownBy(() -> service.assignSubscription(TENANT_ID, req))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("not active");
    }

    // ── 6. Valid state transitions ────────────────────────────────────────────

    @Test
    @DisplayName("activate() transitions TRIAL → ACTIVE")
    void activate_trial_succeeds() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.TRIAL);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantSubscriptionDto result = service.activate(SUB_ID, null);

        assertThat(result.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("suspend() transitions ACTIVE → SUSPENDED")
    void suspend_active_succeeds() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantSubscriptionDto result = service.suspend(SUB_ID, null);

        assertThat(result.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.SUSPENDED);
    }

    @Test
    @DisplayName("reactivate() transitions SUSPENDED → ACTIVE")
    void reactivate_suspended_succeeds() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.SUSPENDED);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantSubscriptionDto result = service.reactivate(SUB_ID, null);

        assertThat(result.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.ACTIVE);
    }

    // ── 7. Invalid state transitions ──────────────────────────────────────────

    @Test
    @DisplayName("suspend() on CANCELLED throws — terminal state")
    void suspend_cancelled_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.CANCELLED);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.suspend(SUB_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid subscription transition");
    }

    @Test
    @DisplayName("activate() on EXPIRED throws — terminal state")
    void activate_expired_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.EXPIRED);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.activate(SUB_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Invalid subscription transition");
    }

    // ── 8. Renew ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("renew() extends period for ACTIVE subscription")
    void renew_active_extendsPeriod() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        LocalDateTime oldEnd = sub.getCurrentPeriodEnd();
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantSubscriptionDto result = service.renew(SUB_ID, null);

        assertThat(result.getCurrentPeriodEnd()).isAfter(oldEnd);
    }

    @Test
    @DisplayName("renew() on SUSPENDED throws")
    void renew_suspended_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.SUSPENDED);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.renew(SUB_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Only ACTIVE or PAST_DUE");
    }

    // ── 9. Plan change ────────────────────────────────────────────────────────

    @Test
    @DisplayName("changePlan() updates plan on active subscription")
    void changePlan_active_succeeds() {
        UUID newPlanId = UUID.randomUUID();
        SubscriptionPlan newPlan = SubscriptionPlan.builder()
                .id(newPlanId).name("Professional")
                .monthlyPriceCents(14900).annualPriceCents(149000)
                .trialDays(14).maxEmployees(500).storageGb(100)
                .enabledModules("HR,PAYROLL,RECRUITMENT")
                .planStatus(SubscriptionPlan.PlanStatus.ACTIVE).build();

        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));
        when(planRepository.findById(newPlanId)).thenReturn(Optional.of(newPlan));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TenantSubscriptionDto result = service.changePlan(SUB_ID, newPlanId, "Upgrade");

        assertThat(result.getPlan().getId()).isEqualTo(newPlanId);
    }

    @Test
    @DisplayName("changePlan() on CANCELLED throws")
    void changePlan_cancelled_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.CANCELLED);
        when(subscriptionRepository.findById(SUB_ID)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.changePlan(SUB_ID, PLAN_ID, null))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("terminal");
    }

    // ── 10. Entitlements — within limit ──────────────────────────────────────

    @Test
    @DisplayName("getEntitlements() returns correct limits when under employee cap")
    void entitlements_underLimit() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.of(sub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(50L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isHasActiveSubscription()).isTrue();
        assertThat(ent.getMaxEmployees()).isEqualTo(100);
        assertThat(ent.getCurrentEmployeeCount()).isEqualTo(50L);
        assertThat(ent.isEmployeeLimitReached()).isFalse();
        assertThat(ent.getEnabledModules()).contains("HR", "PAYROLL");
    }

    // ── 11. Entitlements — at limit ───────────────────────────────────────────

    @Test
    @DisplayName("getEntitlements() flags employeeLimitReached when at cap")
    void entitlements_atLimit() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.of(sub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(100L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isEmployeeLimitReached()).isTrue();
    }

    // ── 12. Entitlements — no subscription ───────────────────────────────────

    @Test
    @DisplayName("getEntitlements() returns no-subscription result when no active sub")
    void entitlements_noSubscription() {
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(0L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isHasActiveSubscription()).isFalse();
        assertThat(ent.isEmployeeLimitReached()).isTrue();
    }

    // ── 13. enforceEmployeeLimit — throws when at limit ───────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws when employee limit reached")
    void enforceEmployeeLimit_atLimit_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID)).thenReturn(Optional.of(sub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(100L);

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Employee limit reached");
    }

    // ── 14. enforceModuleAccess — throws for disabled module ─────────────────

    @Test
    @DisplayName("enforceModuleAccess() throws when module not in plan")
    void enforceModuleAccess_disabledModule_throws() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID)).thenReturn(Optional.of(sub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(0L);

        // Plan only has HR,PAYROLL — RECRUITMENT is not enabled
        assertThatThrownBy(() -> service.enforceModuleAccess(TENANT_ID, "RECRUITMENT"))
                .isInstanceOf(com.platform.tenancy.TenantSecurityException.class)
                .hasMessageContaining("not enabled");
    }

    // ── 15. Plan state machine ────────────────────────────────────────────────

    @Test
    @DisplayName("TenantSubscription.canTransitionTo() — TRIAL can go to ACTIVE")
    void stateMachine_trial_toActive() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.TRIAL);
        assertThat(sub.canTransitionTo(TenantSubscription.SubscriptionStatus.ACTIVE)).isTrue();
    }

    @Test
    @DisplayName("TenantSubscription.canTransitionTo() — EXPIRED cannot transition")
    void stateMachine_expired_noTransition() {
        TenantSubscription sub = activeSub(TenantSubscription.SubscriptionStatus.EXPIRED);
        assertThat(sub.canTransitionTo(TenantSubscription.SubscriptionStatus.ACTIVE)).isFalse();
        assertThat(sub.canTransitionTo(TenantSubscription.SubscriptionStatus.CANCELLED)).isFalse();
    }

    // ── 16. Tenant isolation ──────────────────────────────────────────────────

    @Test
    @DisplayName("getActiveSubscriptionForTenant() throws when tenant not found")
    void getActive_unknownTenant_throws() {
        UUID unknown = UUID.randomUUID();
        when(tenantRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveSubscriptionForTenant(unknown))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Tenant not found");
    }
}
