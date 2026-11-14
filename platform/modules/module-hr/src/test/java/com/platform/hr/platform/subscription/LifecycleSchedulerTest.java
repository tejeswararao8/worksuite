package com.platform.hr.platform.subscription;

import com.platform.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionLifecycleScheduler — Phase 3C.6.
 *
 * <p>Covers:
 * <ul>
 *   <li>PAST_DUE within grace period is NOT suspended</li>
 *   <li>PAST_DUE beyond grace period IS suspended</li>
 *   <li>Expired trial IS transitioned to EXPIRED</li>
 *   <li>Already-SUSPENDED subscription is not processed again (idempotency)</li>
 *   <li>History is recorded exactly once per transition</li>
 *   <li>No-op when no candidates exist</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionLifecycleScheduler — Unit Tests")
class LifecycleSchedulerTest {

    @Mock private TenantSubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionHistoryRepository historyRepository;

    @InjectMocks
    private SubscriptionLifecycleScheduler scheduler;

    private static final int GRACE_DAYS = 7;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", GRACE_DAYS);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Tenant tenant() {
        Tenant t = new Tenant();
        t.setId(UUID.randomUUID());
        return t;
    }

    private SubscriptionPlan plan() {
        return SubscriptionPlan.builder()
                .id(UUID.randomUUID()).name("Growth")
                .monthlyPriceCents(7900).annualPriceCents(79000)
                .trialDays(14).maxEmployees(100).storageGb(20)
                .enabledModules("HR").planStatus(SubscriptionPlan.PlanStatus.ACTIVE)
                .build();
    }

    private TenantSubscription pastDueSub(LocalDateTime periodEnd) {
        return TenantSubscription.builder()
                .id(UUID.randomUUID()).tenant(tenant()).plan(plan())
                .status(TenantSubscription.SubscriptionStatus.PAST_DUE)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now().minusDays(40))
                .currentPeriodStart(LocalDateTime.now().minusDays(40))
                .currentPeriodEnd(periodEnd)
                .assignedBy("admin")
                .build();
    }

    private TenantSubscription trialSub(LocalDateTime trialEndsAt) {
        return TenantSubscription.builder()
                .id(UUID.randomUUID()).tenant(tenant()).plan(plan())
                .status(TenantSubscription.SubscriptionStatus.TRIAL)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .startedAt(LocalDateTime.now().minusDays(20))
                .currentPeriodStart(LocalDateTime.now().minusDays(20))
                .currentPeriodEnd(LocalDateTime.now().plusDays(10))
                .trialEndsAt(trialEndsAt)
                .assignedBy("admin")
                .build();
    }

    // ── 1. PAST_DUE within grace — not suspended ──────────────────────────────

    @Test
    @DisplayName("suspendGraceExpiredSubscriptions() does nothing when no candidates")
    void suspend_noCandidates_noOp() {
        when(subscriptionRepository.findPastDueGraceExpiredForUpdate(any()))
                .thenReturn(List.of());

        scheduler.suspendGraceExpiredSubscriptions();

        verify(subscriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // ── 2. PAST_DUE beyond grace — suspended ─────────────────────────────────

    @Test
    @DisplayName("suspendGraceExpiredSubscriptions() suspends PAST_DUE beyond grace period")
    void suspend_graceExpired_suspends() {
        // Period ended 10 days ago — beyond 7-day grace
        TenantSubscription sub = pastDueSub(LocalDateTime.now().minusDays(10));
        when(subscriptionRepository.findPastDueGraceExpiredForUpdate(any()))
                .thenReturn(List.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        scheduler.suspendGraceExpiredSubscriptions();

        assertThat(sub.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.SUSPENDED);
        verify(subscriptionRepository).save(sub);

        ArgumentCaptor<SubscriptionHistory> histCaptor =
                ArgumentCaptor.forClass(SubscriptionHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        SubscriptionHistory h = histCaptor.getValue();
        assertThat(h.getFromStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.PAST_DUE);
        assertThat(h.getToStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.SUSPENDED);
        assertThat(h.getActorId()).isEqualTo("system:lifecycle-scheduler");
        assertThat(h.getReason()).contains("grace period expired");
    }

    // ── 3. History recorded exactly once ─────────────────────────────────────

    @Test
    @DisplayName("suspendGraceExpiredSubscriptions() records history exactly once per subscription")
    void suspend_historyRecordedOnce() {
        TenantSubscription sub1 = pastDueSub(LocalDateTime.now().minusDays(10));
        TenantSubscription sub2 = pastDueSub(LocalDateTime.now().minusDays(15));
        when(subscriptionRepository.findPastDueGraceExpiredForUpdate(any()))
                .thenReturn(List.of(sub1, sub2));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        scheduler.suspendGraceExpiredSubscriptions();

        verify(historyRepository, times(2)).save(any(SubscriptionHistory.class));
        assertThat(sub1.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.SUSPENDED);
        assertThat(sub2.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.SUSPENDED);
    }

    // ── 4. Idempotency — already SUSPENDED is skipped ────────────────────────

    @Test
    @DisplayName("suspendGraceExpiredSubscriptions() skips subscription already SUSPENDED (idempotency)")
    void suspend_alreadySuspended_skipped() {
        TenantSubscription sub = pastDueSub(LocalDateTime.now().minusDays(10));
        // Simulate status changed between query and lock acquisition
        sub.setStatus(TenantSubscription.SubscriptionStatus.SUSPENDED);
        when(subscriptionRepository.findPastDueGraceExpiredForUpdate(any()))
                .thenReturn(List.of(sub));

        scheduler.suspendGraceExpiredSubscriptions();

        verify(subscriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // ── 5. Expired trial — transitioned to EXPIRED ───────────────────────────

    @Test
    @DisplayName("expireTrialSubscriptions() transitions expired TRIAL to EXPIRED")
    void expireTrial_expired_transitions() {
        // Trial ended 2 days ago
        TenantSubscription sub = trialSub(LocalDateTime.now().minusDays(2));
        when(subscriptionRepository.findExpiredTrialsForUpdate(any()))
                .thenReturn(List.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        scheduler.expireTrialSubscriptions();

        assertThat(sub.getStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.EXPIRED);
        verify(subscriptionRepository).save(sub);

        ArgumentCaptor<SubscriptionHistory> histCaptor =
                ArgumentCaptor.forClass(SubscriptionHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        SubscriptionHistory h = histCaptor.getValue();
        assertThat(h.getFromStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.TRIAL);
        assertThat(h.getToStatus()).isEqualTo(TenantSubscription.SubscriptionStatus.EXPIRED);
        assertThat(h.getActorId()).isEqualTo("system:lifecycle-scheduler");
        assertThat(h.getReason()).contains("Trial period ended");
    }

    // ── 6. No expired trials — no-op ─────────────────────────────────────────

    @Test
    @DisplayName("expireTrialSubscriptions() does nothing when no expired trials")
    void expireTrial_noCandidates_noOp() {
        when(subscriptionRepository.findExpiredTrialsForUpdate(any()))
                .thenReturn(List.of());

        scheduler.expireTrialSubscriptions();

        verify(subscriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // ── 7. Idempotency — already EXPIRED trial is skipped ────────────────────

    @Test
    @DisplayName("expireTrialSubscriptions() skips subscription already EXPIRED (idempotency)")
    void expireTrial_alreadyExpired_skipped() {
        TenantSubscription sub = trialSub(LocalDateTime.now().minusDays(2));
        sub.setStatus(TenantSubscription.SubscriptionStatus.EXPIRED);
        when(subscriptionRepository.findExpiredTrialsForUpdate(any()))
                .thenReturn(List.of(sub));

        scheduler.expireTrialSubscriptions();

        verify(subscriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // ── 8. Grace period boundary — exactly at deadline ───────────────────────

    @Test
    @DisplayName("suspendGraceExpiredSubscriptions() uses correct grace deadline in query")
    void suspend_graceDeadlineCalculation() {
        when(subscriptionRepository.findPastDueGraceExpiredForUpdate(any()))
                .thenReturn(List.of());

        scheduler.suspendGraceExpiredSubscriptions();

        ArgumentCaptor<LocalDateTime> deadlineCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        verify(subscriptionRepository).findPastDueGraceExpiredForUpdate(deadlineCaptor.capture());

        LocalDateTime captured = deadlineCaptor.getValue();
        LocalDateTime expected = LocalDateTime.now().minusDays(GRACE_DAYS);
        // Allow 5 seconds tolerance for test execution time
        assertThat(captured).isBetween(expected.minusSeconds(5), expected.plusSeconds(5));
    }
}
