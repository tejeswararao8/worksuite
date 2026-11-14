package com.platform.hr.subscription;

import com.platform.document.MinioStorageService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Wraps {@link MinioStorageService} with subscription storage quota enforcement.
 *
 * <p>All tenant file uploads MUST go through this service rather than calling
 * {@code MinioStorageService.store()} directly. This ensures:
 * <ol>
 *   <li>The quota is checked before the upload is accepted.</li>
 *   <li>The file size is measured server-side from the actual stream — never
 *       trusted from the browser's Content-Length or form field.</li>
 *   <li>Storage usage is recorded atomically after a successful upload.</li>
 *   <li>Storage usage is released when a file is deleted.</li>
 * </ol>
 *
 * <p>Platform admins bypass quota checks (they are not bound to a tenant subscription).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaAwareStorageService {

    private final MinioStorageService minioStorageService;
    private final SubscriptionService subscriptionService;

    /**
     * Stores a file in MinIO after enforcing the tenant's storage quota.
     *
     * @param file       the uploaded file (size measured server-side)
     * @param entityType logical entity type (e.g. "employees", "documents")
     * @param entityId   entity UUID string
     * @return the MinIO object path (stored in the document record)
     * @throws com.platform.tenancy.TenantException if the quota would be exceeded
     */
    @Transactional
    public String store(MultipartFile file, String entityType, String entityId) throws Exception {
        String tenantIdStr = TenantContext.getTenantId();
        UUID tenantId = tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;

        // Measure actual file size server-side — never trust browser-supplied size.
        long fileSizeBytes = file.getSize();

        // Enforce quota before writing to MinIO.
        if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            subscriptionService.enforceStorageQuota(tenantId, fileSizeBytes);
        }

        String objectPath = minioStorageService.store(file, tenantIdStr, entityType, entityId);

        // Record usage only after the upload succeeds.
        if (tenantId != null && !TenantSecurityService.PLATFORM_TENANT_ID.equals(tenantId)) {
            subscriptionService.recordStorageUsed(tenantId, fileSizeBytes);
        }

        log.debug("Stored file: tenant={} path={} size={}", tenantIdStr, objectPath, fileSizeBytes);
        return objectPath;
    }

    /**
     * Deletes a file from MinIO and releases the storage quota.
     *
     * @param objectPath    the MinIO object path
     * @param fileSizeBytes the size of the file being deleted (from the document record)
     */
    @Transactional
    public void delete(String objectPath, long fileSizeBytes) throws Exception {
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
}
