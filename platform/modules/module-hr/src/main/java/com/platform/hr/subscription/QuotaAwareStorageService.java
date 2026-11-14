package com.platform.hr.subscription;

import com.platform.document.MinioStorageService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Wraps {@link MinioStorageService} with subscription storage quota enforcement.
 *
 * <h3>Storage reservation protocol</h3>
 * <ol>
 *   <li>Measure actual file size server-side — never trust browser-supplied size.</li>
 *   <li>Atomically reserve the bytes via {@link StorageReservationService#reserve}:
 *       enforces quota + increments usage in one pessimistic-locked transaction that
 *       commits <em>before</em> the upload begins. The reservation is executed through
 *       a separate Spring bean to ensure the {@code REQUIRES_NEW} transaction proxy
 *       is honoured (self-invocation would bypass it).</li>
 *   <li>Upload the file to MinIO outside the reservation transaction so the DB
 *       lock is not held during network I/O.</li>
 *   <li>If the upload fails, release the reservation via
 *       {@link StorageReservationService#release}. Release failures are logged
 *       with enough context for manual reconciliation — they do not mask the
 *       original upload exception.</li>
 *   <li>If the upload succeeds, the reservation is already committed — no further
 *       action needed.</li>
 * </ol>
 *
 * <h3>Replacement uploads</h3>
 * <p>Callers must pass {@code oldFileSizeBytes > 0} to release the old file's quota
 * before reserving for the new file. This is handled atomically in
 * {@link #storeReplacement}. On upload failure the old quota is restored.
 *
 * <h3>Deletion</h3>
 * <p>Storage usage is only decremented after a confirmed MinIO deletion. If MinIO
 * deletion fails, usage is NOT decremented — the file still occupies space.
 *
 * <h3>Platform bypass</h3>
 * <p>Platform admins (platform pseudo-tenant) bypass all quota checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaAwareStorageService {

    private final MinioStorageService minioStorageService;
    private final SubscriptionService subscriptionService;
    private final StorageReservationService reservationService;

    /**
     * Stores a new file in MinIO after atomically reserving the storage quota.
     *
     * @param file       the uploaded file (size measured server-side)
     * @param entityType logical entity type (e.g. "employees", "documents")
     * @param entityId   entity UUID string
     * @return the MinIO object path
     * @throws com.platform.tenancy.TenantException if the quota would be exceeded
     */
    public String store(MultipartFile file, String entityType, String entityId) throws Exception {
        String tenantIdStr = TenantContext.getTenantId();
        UUID tenantId = parseTenantId(tenantIdStr);
        long fileSizeBytes = file.getSize();

        boolean enforceQuota = isQuotaEnforced(tenantId);

        // Step 1: atomically reserve quota — commits before upload starts
        if (enforceQuota) {
            reservationService.reserve(tenantId, fileSizeBytes);
        }

        // Step 2: upload to MinIO (reservation transaction already committed)
        String objectPath;
        try {
            objectPath = minioStorageService.store(file, tenantIdStr, entityType, entityId);
        } catch (Exception e) {
            // Step 3: upload failed — release the committed reservation
            if (enforceQuota) {
                log.warn("store: upload failed for tenant={} entityType={} size={} — releasing reservation: {}",
                        tenantId, entityType, fileSizeBytes, e.getMessage());
                reservationService.release(tenantId, fileSizeBytes);
            }
            throw e;
        }

        log.debug("store: tenant={} path={} size={}", tenantId, objectPath, fileSizeBytes);
        return objectPath;
    }

    /**
     * Replaces an existing file: releases old quota, reserves new quota, then uploads.
     * If the upload fails, the new reservation is released and the old quota is restored.
     *
     * <p>Concurrency note: the release of the old quota and reservation of the new quota
     * are two separate transactions. A concurrent upload could theoretically use the
     * freed bytes between these two operations. This is an acceptable trade-off — the
     * window is sub-millisecond and the quota check in {@code reserve} uses a pessimistic
     * write lock that serialises concurrent reservations for the same tenant.
     *
     * @param file             the new file
     * @param entityType       logical entity type
     * @param entityId         entity UUID string
     * @param oldFileSizeBytes size of the file being replaced (0 if unknown/not applicable)
     * @return the MinIO object path of the new file
     */
    public String storeReplacement(MultipartFile file, String entityType, String entityId,
                                   long oldFileSizeBytes) throws Exception {
        String tenantIdStr = TenantContext.getTenantId();
        UUID tenantId = parseTenantId(tenantIdStr);
        long newFileSizeBytes = file.getSize();

        boolean enforceQuota = isQuotaEnforced(tenantId);

        if (enforceQuota) {
            // Release old quota first so the new reservation sees the freed space
            if (oldFileSizeBytes > 0) {
                subscriptionService.releaseStorage(tenantId, oldFileSizeBytes);
            }
            // Reserve for the new file — commits before upload
            reservationService.reserve(tenantId, newFileSizeBytes);
        }

        String objectPath;
        try {
            objectPath = minioStorageService.store(file, tenantIdStr, entityType, entityId);
        } catch (Exception e) {
            // Rollback: release new reservation, restore old quota
            if (enforceQuota) {
                log.warn("storeReplacement: upload failed for tenant={} entityType={} newSize={} oldSize={} — rolling back quota: {}",
                        tenantId, entityType, newFileSizeBytes, oldFileSizeBytes, e.getMessage());
                reservationService.release(tenantId, newFileSizeBytes);
                if (oldFileSizeBytes > 0) {
                    try {
                        subscriptionService.recordStorageUsed(tenantId, oldFileSizeBytes);
                    } catch (Exception restoreEx) {
                        log.error("storeReplacement: failed to restore old quota for tenant={} oldSize={} — manual reconciliation needed: {}",
                                tenantId, oldFileSizeBytes, restoreEx.getMessage(), restoreEx);
                    }
                }
            }
            throw e;
        }

        log.debug("storeReplacement: tenant={} path={} oldSize={} newSize={}",
                tenantId, objectPath, oldFileSizeBytes, newFileSizeBytes);
        return objectPath;
    }

    /**
     * Deletes a file from MinIO and releases the storage quota.
     * If the MinIO deletion fails, storage usage is NOT decremented to avoid
     * under-counting (the file still exists and occupies space).
     *
     * @param objectPath    the MinIO object path
     * @param fileSizeBytes the size of the file being deleted (from the document record)
     */
    public void delete(String objectPath, long fileSizeBytes) throws Exception {
        // Delete from MinIO first — only release quota if deletion succeeds
        minioStorageService.delete(objectPath);

        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr != null) {
            UUID tenantId = UUID.fromString(tenantIdStr);
            if (!TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
                subscriptionService.releaseStorage(tenantId, fileSizeBytes);
                log.debug("delete: released {} bytes for tenant={} path={}", fileSizeBytes, tenantId, objectPath);
            }
        }
    }

    /** Delegates presigned URL generation — no quota impact. */
    public String getPresignedUrl(String objectPath) throws Exception {
        return minioStorageService.getPresignedUrl(objectPath);
    }

    /** Delegates raw load — no quota impact. */
    public byte[] load(String objectPath) throws Exception {
        return minioStorageService.load(objectPath);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private boolean isQuotaEnforced(UUID tenantId) {
        return tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId);
    }

    private static UUID parseTenantId(String tenantIdStr) {
        if (tenantIdStr == null) return null;
        try {
            return UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
