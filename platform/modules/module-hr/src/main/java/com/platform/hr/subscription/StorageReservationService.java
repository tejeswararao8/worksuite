package com.platform.hr.subscription;

import com.platform.hr.platform.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Isolated service for atomic storage quota reservation.
 *
 * <p>Exists as a separate Spring bean so that {@link #reserve} executes in its
 * own transaction via the Spring proxy. Calling {@code @Transactional(REQUIRES_NEW)}
 * from within the same bean (self-invocation) bypasses the proxy and the annotation
 * has no effect — this class eliminates that problem.
 *
 * <p>Protocol:
 * <ol>
 *   <li>{@link #reserve} acquires a pessimistic write lock, checks the quota, and
 *       increments usage — all in one committed transaction. The lock is released
 *       when the transaction commits, before MinIO upload begins.</li>
 *   <li>If the upload fails, the caller calls {@link #release} to decrement usage.</li>
 *   <li>{@link #release} is safe to call even if the reservation partially failed —
 *       the decrement is floored at 0 by the repository query.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageReservationService {

    private final SubscriptionService subscriptionService;

    /**
     * Atomically enforces the storage quota and increments usage in a new,
     * immediately-committed transaction.
     *
     * <p>Uses {@code REQUIRES_NEW} so the transaction commits (and the DB lock
     * is released) before the caller proceeds to the MinIO upload. This prevents
     * the DB connection from being held open during network I/O.
     *
     * @param tenantId      the tenant performing the upload
     * @param fileSizeBytes server-measured file size in bytes
     * @throws com.platform.tenancy.TenantException if the quota would be exceeded
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(UUID tenantId, long fileSizeBytes) {
        subscriptionService.enforceStorageQuota(tenantId, fileSizeBytes);
        subscriptionService.recordStorageUsed(tenantId, fileSizeBytes);
        log.debug("StorageReservation: reserved {} bytes for tenant={}", fileSizeBytes, tenantId);
    }

    /**
     * Releases a previously committed reservation by decrementing usage.
     * Safe to call on upload failure — logs but does not throw.
     *
     * @param tenantId      the tenant that owns the reservation
     * @param fileSizeBytes the number of bytes to release
     */
    public void release(UUID tenantId, long fileSizeBytes) {
        try {
            subscriptionService.releaseStorage(tenantId, fileSizeBytes);
            log.debug("StorageReservation: released {} bytes for tenant={}", fileSizeBytes, tenantId);
        } catch (Exception ex) {
            log.error("StorageReservation: failed to release {} bytes for tenant={} — manual reconciliation may be needed: {}",
                    fileSizeBytes, tenantId, ex.getMessage(), ex);
        }
    }
}
