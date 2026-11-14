package com.platform.hr.platform.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled subscription lifecycle processor.
 *
 * <p>Runs periodically to close lifecycle gaps that cannot be handled
 * synchronously at request time:
 * <ol>
 *   <li>PAST_DUE → SUSPENDED: subscriptions whose grace period has expired.</li>
 *   <li>TRIAL → EXPIRED: subscriptions whose trial period has ended.</li>
 * </ol>
 *
 * <p>Concurrency safety:
 * <ul>
 *   <li>Each batch is processed in a single transaction with a pessimistic
 *       write lock on the selected rows, preventing duplicate processing
 *       within a single DB instance.</li>
 *   <li>{@code @Scheduled} with a fixed delay (not rate) ensures the next
 *       run only starts after the previous one completes, preventing
 *       overlapping executions within the same JVM instance.</li>
 *   <li>For multi-instance deployments, the pessimistic row-level lock
 *       ensures only one instance processes each subscription row at a time.
 *       The idempotent status check (only PAST_DUE rows are selected for
 *       suspension, only TRIAL rows for expiry) prevents double-processing.</li>
 * </ul>
 *
 * <p>History integrity: every transition writes exactly one history record
 * inside the same transaction as the status update.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionLifecycleScheduler {

    private static final String SYSTEM_ACTOR = "system:lifecycle-scheduler";

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;

    @Value("${platform.subscription.grace-period-days:7}")
    private int gracePeriodDays;

    /**
     * Suspends PAST_DUE subscriptions whose grace period has expired.
     * Runs every hour (fixed delay — next run starts after current completes).
     */
    @Scheduled(fixedDelayString = "${platform.subscription.scheduler.past-due-delay-ms:3600000}")
    @Transactional
    public void suspendGraceExpiredSubscriptions() {
        // Grace deadline: any subscription with currentPeriodEnd before this
        // timestamp has exhausted its grace period.
        LocalDateTime graceDeadline = LocalDateTime.now().minusDays(gracePeriodDays);

        List<TenantSubscription> candidates =
                subscriptionRepository.findPastDueGraceExpiredForUpdate(graceDeadline);

        if (candidates.isEmpty()) {
            log.debug("LifecycleScheduler: no PAST_DUE subscriptions to suspend");
            return;
        }

        log.info("LifecycleScheduler: suspending {} grace-expired PAST_DUE subscription(s)",
                candidates.size());

        for (TenantSubscription sub : candidates) {
            // Idempotency guard — status may have changed between query and lock
            if (sub.getStatus() != TenantSubscription.SubscriptionStatus.PAST_DUE) {
                log.debug("LifecycleScheduler: skipping sub={} — status is now {}",
                        sub.getId(), sub.getStatus());
                continue;
            }

            TenantSubscription.SubscriptionStatus prev = sub.getStatus();
            sub.setStatus(TenantSubscription.SubscriptionStatus.SUSPENDED);
            subscriptionRepository.save(sub);

            appendHistory(sub, prev, TenantSubscription.SubscriptionStatus.SUSPENDED,
                    "Automatically suspended: grace period expired after " +
                    gracePeriodDays + " days past due");

            log.info("LifecycleScheduler: suspended subscription id={} tenantId={}",
                    sub.getId(), sub.getTenant().getId());
        }
    }

    /**
     * Expires TRIAL subscriptions whose trial period has ended.
     * Runs every hour (fixed delay).
     */
    @Scheduled(fixedDelayString = "${platform.subscription.scheduler.trial-delay-ms:3600000}")
    @Transactional
    public void expireTrialSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<TenantSubscription> candidates =
                subscriptionRepository.findExpiredTrialsForUpdate(now);

        if (candidates.isEmpty()) {
            log.debug("LifecycleScheduler: no expired TRIAL subscriptions");
            return;
        }

        log.info("LifecycleScheduler: expiring {} trial subscription(s)", candidates.size());

        for (TenantSubscription sub : candidates) {
            // Idempotency guard
            if (sub.getStatus() != TenantSubscription.SubscriptionStatus.TRIAL) {
                log.debug("LifecycleScheduler: skipping sub={} — status is now {}",
                        sub.getId(), sub.getStatus());
                continue;
            }

            TenantSubscription.SubscriptionStatus prev = sub.getStatus();
            sub.setStatus(TenantSubscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            appendHistory(sub, prev, TenantSubscription.SubscriptionStatus.EXPIRED,
                    "Trial period ended: trialEndsAt=" + sub.getTrialEndsAt());

            log.info("LifecycleScheduler: expired trial subscription id={} tenantId={}",
                    sub.getId(), sub.getTenant().getId());
        }
    }

    private void appendHistory(TenantSubscription sub,
                                TenantSubscription.SubscriptionStatus fromStatus,
                                TenantSubscription.SubscriptionStatus toStatus,
                                String reason) {
        SubscriptionHistory h = SubscriptionHistory.builder()
                .subscriptionId(sub.getId())
                .tenantId(sub.getTenant().getId())
                .planId(sub.getPlan().getId())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .fromPlanId(sub.getPlan().getId())
                .toPlanId(sub.getPlan().getId())
                .actorId(SYSTEM_ACTOR)
                .reason(reason)
                .build();
        historyRepository.save(h);
    }
}
