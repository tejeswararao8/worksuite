package com.platform.hr.platform.subscription;

import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantRepository;
import com.platform.tenancy.TenantSecurityException;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Phase 3C.5 enforcement methods in SubscriptionService.
 *
 * <p>Covers:
 * <ul>
 *   <li>Employee creation within limits</li>
 *   <li>Employee creation exceeding limits</li>
 *   <li>Bulk import exceeding limits (same enforceEmployeeLimit path)</li>
 *   <li>Enabled and disabled module access</li>
 *   <li>Expired subscriptions</li>
 *   <li>Suspended subscriptions</li>
 *   <li>PAST_DUE grace-period behavior</li>
 *   <li>Storage upload within quota</li>
 *   <li>Storage upload exceeding quota</li>
 *   <li>Tenant isolation (platform pseudo-tenant bypass)</li>
 *   <li>Platform administrator access bypass</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService — Phase 3C.5 Enforcement Unit Tests")
class EnforcementServiceTest {

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

    @BeforeEach
    void setupAuth() {
        ReflectionTestUtils.setField(service, "gracePeriodDays", 7);
        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getSubject()).thenReturn("test-actor");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SubscriptionPlan plan(int maxEmployees, int storageGb, String modules) {
        return SubscriptionPlan.builder()
                .id(PLAN_ID).name("Growth")
                .monthlyPriceCents(7900).annualPriceCents(79000)
                .trialDays(14).maxEmployees(maxEmployees).storageGb(storageGb)
                .enabledModules(modules)
                .planStatus(SubscriptionPlan.PlanStatus.ACTIVE)
                .build();
    }

    private TenantSubscription sub(TenantSubscription.SubscriptionStatus status,
                                   SubscriptionPlan plan,
                                   long storageUsedBytes) {
        return sub(status, plan, storageUsedBytes, LocalDateTime.now().plusDays(20));
    }

    private TenantSubscription sub(TenantSubscription.SubscriptionStatus status,
                                   SubscriptionPlan plan,
                                   long storageUsedBytes,
                                   LocalDateTime periodEnd) {
        com.platform.tenancy.Tenant tenant = new com.platform.tenancy.Tenant();
        tenant.setId(TENANT_ID);
        return TenantSubscription.builder()
                .id(SUB_ID)
                .tenant(tenant)
                .plan(plan)
                .status(status)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now().minusDays(10))
                .currentPeriodStart(LocalDateTime.now().minusDays(10))
                .currentPeriodEnd(periodEnd)
                .storageUsedBytes(storageUsedBytes)
                .assignedBy("admin")
                .build();
    }

    // ── 1. Employee limit — within limit ──────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() passes when under employee cap")
    void employeeLimit_underCap_passes() {
        SubscriptionPlan p = plan(100, 20, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(50L);

        assertThatNoException().isThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID));
    }

    // ── 2. Employee limit — at cap ────────────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws when at employee cap")
    void employeeLimit_atCap_throws() {
        SubscriptionPlan p = plan(100, 20, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(100L);

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Employee limit reached")
                .hasMessageContaining("100")
                .hasMessageContaining("Growth");
    }

    // ── 3. Employee limit — exceeds cap ───────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws when exceeding employee cap (bulk import scenario)")
    void employeeLimit_exceedsCap_throws() {
        SubscriptionPlan p = plan(25, 5, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(25L);

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Employee limit reached")
                .hasMessageContaining("Limit: 25");
    }

    // ── 4. Employee limit — unlimited plan ────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() passes for unlimited plan (-1)")
    void employeeLimit_unlimited_passes() {
        SubscriptionPlan p = plan(-1, -1, "HR,PAYROLL");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));

        assertThatNoException().isThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID));
        verifyNoInteractions(membershipRepository);
    }

    // ── 5. Employee limit — no subscription ───────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws when no active subscription")
    void employeeLimit_noSubscription_throws() {
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("no active subscription");
    }

    // ── 6. Module access — enabled ────────────────────────────────────────────

    @Test
    @DisplayName("enforceModuleAccess() passes for enabled module")
    void moduleAccess_enabled_passes() {
        SubscriptionPlan p = plan(100, 20, "HR,PAYROLL,RECRUITMENT");
        TenantSubscription activeSub = sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(activeSub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(10L);

        assertThatNoException().isThrownBy(() -> service.enforceModuleAccess(TENANT_ID, "PAYROLL"));
    }

    // ── 7. Module access — disabled ───────────────────────────────────────────

    @Test
    @DisplayName("enforceModuleAccess() throws TenantSecurityException for disabled module")
    void moduleAccess_disabled_throws() {
        SubscriptionPlan p = plan(100, 20, "HR");
        TenantSubscription activeSub = sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(activeSub));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(10L);

        assertThatThrownBy(() -> service.enforceModuleAccess(TENANT_ID, "PAYROLL"))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("PAYROLL")
                .hasMessageContaining("not enabled");
    }

    // ── 8. Subscription status — SUSPENDED blocks operations ─────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws for SUSPENDED subscription")
    void suspended_blocksEmployeeCreation() {
        SubscriptionPlan p = plan(100, 20, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.SUSPENDED, p, 0)));

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("suspended");
    }

    // ── 9. Subscription status — EXPIRED blocks operations ───────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws for EXPIRED subscription")
    void expired_blocksEmployeeCreation() {
        SubscriptionPlan p = plan(100, 20, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.EXPIRED, p, 0)));

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("expired");
    }

    // ── 10. PAST_DUE — within grace period allows operations ─────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() passes for PAST_DUE within grace period")
    void pastDue_withinGrace_passes() {
        SubscriptionPlan p = plan(100, 20, "HR");
        // Period ended 2 days ago — still within 7-day grace period
        LocalDateTime periodEnd = LocalDateTime.now().minusDays(2);
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.PAST_DUE, p, 0, periodEnd)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(10L);

        assertThatNoException().isThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID));
    }

    // ── 11. PAST_DUE — grace period expired blocks operations ────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() throws for PAST_DUE after grace period")
    void pastDue_graceExpired_throws() {
        SubscriptionPlan p = plan(100, 20, "HR");
        // Period ended 10 days ago — beyond 7-day grace period
        LocalDateTime periodEnd = LocalDateTime.now().minusDays(10);
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.PAST_DUE, p, 0, periodEnd)));

        assertThatThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("grace period has expired");
    }

    // ── 12. Storage quota — within quota ─────────────────────────────────────

    @Test
    @DisplayName("enforceStorageQuota() passes when upload fits within quota")
    void storageQuota_withinQuota_passes() {
        SubscriptionPlan p = plan(100, 5, "HR"); // 5 GB quota
        long usedBytes = 1L * 1024 * 1024 * 1024; // 1 GB used
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, usedBytes)));

        long uploadSize = 100L * 1024 * 1024; // 100 MB upload
        assertThatNoException().isThrownBy(() -> service.enforceStorageQuota(TENANT_ID, uploadSize));
    }

    // ── 13. Storage quota — exceeds quota ────────────────────────────────────

    @Test
    @DisplayName("enforceStorageQuota() throws when upload would exceed quota")
    void storageQuota_exceedsQuota_throws() {
        SubscriptionPlan p = plan(100, 5, "HR"); // 5 GB quota
        long usedBytes = 4L * 1024 * 1024 * 1024 + 900L * 1024 * 1024; // ~4.9 GB used
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, usedBytes)));

        long uploadSize = 200L * 1024 * 1024; // 200 MB upload — would exceed 5 GB
        assertThatThrownBy(() -> service.enforceStorageQuota(TENANT_ID, uploadSize))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Storage quota exceeded")
                .hasMessageContaining("5 GB");
    }

    // ── 14. Storage quota — unlimited plan ───────────────────────────────────

    @Test
    @DisplayName("enforceStorageQuota() passes for unlimited storage plan (-1)")
    void storageQuota_unlimited_passes() {
        SubscriptionPlan p = plan(-1, -1, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));

        assertThatNoException().isThrownBy(() ->
                service.enforceStorageQuota(TENANT_ID, 10L * 1024 * 1024 * 1024));
    }

    // ── 15. Platform admin bypass ─────────────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() bypasses check for platform pseudo-tenant")
    void platformTenant_bypassesEmployeeLimit() {
        assertThatNoException().isThrownBy(() ->
                service.enforceEmployeeLimit(TenantSecurityService.PLATFORM_TENANT_ID));
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("enforceStorageQuota() bypasses check for platform pseudo-tenant")
    void platformTenant_bypassesStorageQuota() {
        assertThatNoException().isThrownBy(() ->
                service.enforceStorageQuota(TenantSecurityService.PLATFORM_TENANT_ID, Long.MAX_VALUE));
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("enforceModuleAccess() bypasses check for platform pseudo-tenant")
    void platformTenant_bypassesModuleAccess() {
        assertThatNoException().isThrownBy(() ->
                service.enforceModuleAccess(TenantSecurityService.PLATFORM_TENANT_ID, "PAYROLL"));
        verifyNoInteractions(subscriptionRepository);
    }

    // ── 16. Tenant isolation ──────────────────────────────────────────────────

    @Test
    @DisplayName("enforceEmployeeLimit() uses only the requesting tenant's subscription")
    void tenantIsolation_usesCorrectTenant() {
        UUID otherTenant = UUID.randomUUID();
        SubscriptionPlan p = plan(100, 20, "HR");
        when(subscriptionRepository.findActiveByTenantIdForUpdate(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, 0)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(10L);

        assertThatNoException().isThrownBy(() -> service.enforceEmployeeLimit(TENANT_ID));

        // Verify the OTHER tenant's subscription was never queried
        verify(subscriptionRepository, never()).findActiveByTenantIdForUpdate(otherTenant);
        verify(membershipRepository, never())
                .countByTenantIdAndMembershipStatus(eq(otherTenant), any());
    }

    // ── 17. getEntitlements — grace period fields ─────────────────────────────

    @Test
    @DisplayName("getEntitlements() populates inGracePeriod=true for PAST_DUE within grace")
    void entitlements_pastDue_withinGrace_flagsGracePeriod() {
        SubscriptionPlan p = plan(100, 20, "HR");
        LocalDateTime periodEnd = LocalDateTime.now().minusDays(3);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.PAST_DUE, p, 0, periodEnd)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(5L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isInGracePeriod()).isTrue();
        assertThat(ent.getGracePeriodDaysRemaining()).isGreaterThan(0);
        assertThat(ent.isOperationallyActive()).isTrue();
    }

    @Test
    @DisplayName("getEntitlements() populates inGracePeriod=false for PAST_DUE after grace")
    void entitlements_pastDue_afterGrace_notOperational() {
        SubscriptionPlan p = plan(100, 20, "HR");
        LocalDateTime periodEnd = LocalDateTime.now().minusDays(10);
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.PAST_DUE, p, 0, periodEnd)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(5L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isInGracePeriod()).isFalse();
        assertThat(ent.isOperationallyActive()).isFalse();
    }

    // ── 18. getEntitlements — storage usage fields ────────────────────────────

    @Test
    @DisplayName("getEntitlements() returns correct storage usage and quota fields")
    void entitlements_storageFields() {
        SubscriptionPlan p = plan(100, 10, "HR"); // 10 GB quota
        long usedBytes = 3L * 1024 * 1024 * 1024; // 3 GB used
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, usedBytes)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(20L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.getStorageGb()).isEqualTo(10);
        assertThat(ent.getStorageUsedBytes()).isEqualTo(usedBytes);
        assertThat(ent.isStorageQuotaReached()).isFalse();
    }

    @Test
    @DisplayName("getEntitlements() flags storageQuotaReached when at quota")
    void entitlements_storageAtQuota_flagged() {
        SubscriptionPlan p = plan(100, 5, "HR"); // 5 GB quota
        long quotaBytes = 5L * 1024 * 1024 * 1024;
        when(subscriptionRepository.findActiveByTenantId(TENANT_ID))
                .thenReturn(Optional.of(sub(TenantSubscription.SubscriptionStatus.ACTIVE, p, quotaBytes)));
        when(membershipRepository.countByTenantIdAndMembershipStatus(TENANT_ID, MembershipStatus.ACTIVE))
                .thenReturn(20L);

        SubscriptionEntitlementDto ent = service.getEntitlements(TENANT_ID);

        assertThat(ent.isStorageQuotaReached()).isTrue();
    }
}
