package com.platform.hr.subscription;

import com.platform.document.MinioStorageService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Wraps {@link MinioStorageService} with subscription storage quota enforcement.
 *
 * <p>Storage reservation protocol (prevents concurrent over-allocation):
 * <ol>
 *   <li>Measure actual file size server-side — never trust browser-supplied size.</li>
 *   <li>Atomically reserve the bytes: enforce quota check + increment usage in one
 *       pessimistic-locked transaction. If the quota would be exceeded, the
 *       reservation is rejected before any upload occurs.</li>
 *   <li>Upload the file to MinIO outside the reservation transaction so the DB
 *       lock is not held during the network I/O.</li>
 *   <li>If the upload fails, release the reservation atomically.</li>
 *   <li>If the upload succeeds, the reservation is already committed — no further
 *       action needed.</li>
 * </ol>
 *
 * <p>Replacement uploads: callers must pass {@code oldFileSizeBytes > 0} to
 * release the old file's quota before reserving for the new file. This is
 * handled atomically in {@link #storeReplacement}.
 *
 * <p>Platform admins bypass quota checks (not bound to a tenant subscription).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaAwareStorageService {

    private final MinioStorageService minioStorageService;
    private final SubscriptionService subscriptionService;

    /**
     * Stores a new file in MinIO after atomically reserving the storage quota.
     *
     * @param file       the uploaded file (size measured server-side)
     * @param entityType logical entity type (e.g. "employees", "documents")
     * @param entityId   entity UUID string
     * @return the MinIO object path
     * @throws TenantException if the quota would be exceeded
     */
    public String store(MultipartFile file, String entityType, String entityId) throws Exception {
        String tenantIdStr = TenantContext.getTenantId();
        UUID tenantId = parseTenantId(tenantIdStr);

        long fileSizeBytes = file.getSize();

        // Step 1: atomically reserve quota (enforce + increment in one locked tx)
        if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            reserveStorage(tenantId, fileSizeBytes);
        }

        // Step 2: upload to MinIO (outside the reservation transaction)
        String objectPath;
        try {
            objectPath = minioStorageService.store(file, tenantIdStr, entityType, entityId);
        } catch (Exception e) {
            // Step 3: upload failed — release the reservation
            if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
                releaseStorageSafe(tenantId, fileSizeBytes);
            }
            throw e;
        }

        log.debug("Stored file: tenant={} path={} size={}", tenantIdStr, objectPath, fileSizeBytes);
        return objectPath;
    }

    /**
     * Replaces an existing file: releases old quota, reserves new quota, then uploads.
     * If the upload fails, the new reservation is released and the old quota is restored.
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

        if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            // Release old quota first, then reserve for new file
            if (oldFileSizeBytes > 0) {
                subscriptionService.releaseStorage(tenantId, oldFileSizeBytes);
            }
            reserveStorage(tenantId, newFileSizeBytes);
        }

        String objectPath;
        try {
            objectPath = minioStorageService.store(file, tenantIdStr, entityType, entityId);
        } catch (Exception e) {
            // Rollback: release new reservation, restore old quota
            if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
                releaseStorageSafe(tenantId, newFileSizeBytes);
                if (oldFileSizeBytes > 0) {
                    subscriptionService.recordStorageUsed(tenantId, oldFileSizeBytes);
                }
            }
            throw e;
        }

        log.debug("Replaced file: tenant={} path={} oldSize={} newSize={}",
                tenantIdStr, objectPath, oldFileSizeBytes, newFileSizeBytes);
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

    /**
     * Atomically enforces quota and increments usage in a single pessimistic-locked
     * transaction. This is the reservation step — it must complete before the upload
     * begins so concurrent uploads cannot both pass the quota check.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveStorage(UUID tenantId, long fileSizeBytes) {
        // enforceStorageQuota acquires a pessimistic write lock and checks the quota
        subscriptionService.enforceStorageQuota(tenantId, fileSizeBytes);
        // Immediately increment — the lock is still held, preventing races
        subscriptionService.recordStorageUsed(tenantId, fileSizeBytes);
    }

    /**
     * Releases a storage reservation safely — logs but does not throw on failure
     * to avoid masking the original upload exception.
     */
    private void releaseStorageSafe(UUID tenantId, long fileSizeBytes) {
        try {
            subscriptionService.releaseStorage(tenantId, fileSizeBytes);
        } catch (Exception ex) {
            log.error("Failed to release storage reservation for tenant={} size={}: {}",
                    tenantId, fileSizeBytes, ex.getMessage(), ex);
        }
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
