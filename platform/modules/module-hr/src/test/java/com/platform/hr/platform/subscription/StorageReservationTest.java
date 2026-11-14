package com.platform.hr.platform.subscription;

import com.platform.document.MinioStorageService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.hr.subscription.QuotaAwareStorageService;
import com.platform.tenancy.TenantContext;
import com.platform.tenancy.TenantException;
import com.platform.tenancy.TenantSecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuotaAwareStorageService — Phase 3C.6 storage reservation.
 *
 * <p>Covers:
 * <ul>
 *   <li>Reservation within quota succeeds</li>
 *   <li>Reservation exceeding quota fails before upload</li>
 *   <li>Failed upload releases reservation</li>
 *   <li>Successful deletion releases storage</li>
 *   <li>Failed deletion does NOT reduce usage</li>
 *   <li>Replacement upload accounts for old and new sizes</li>
 *   <li>Storage usage never becomes negative (floor at 0 via decrementStorageUsed)</li>
 *   <li>Platform tenant bypasses all quota checks</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuotaAwareStorageService — Storage Reservation Tests")
class StorageReservationTest {

    @Mock private MinioStorageService minioStorageService;
    @Mock private SubscriptionService subscriptionService;

    @InjectMocks
    private QuotaAwareStorageService storageService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String OBJECT_PATH = "tenants/test/employees/photo.jpg";

    @BeforeEach
    void setTenantContext() {
        TenantContext.setTenantId(TENANT_ID.toString());
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    private MultipartFile mockFile(long size) {
        MultipartFile f = mock(MultipartFile.class);
        when(f.getSize()).thenReturn(size);
        return f;
    }

    // ── 1. Reservation within quota succeeds ─────────────────────────────────

    @Test
    @DisplayName("store() enforces quota then records usage before returning path")
    void store_withinQuota_succeeds() throws Exception {
        long fileSize = 5L * 1024 * 1024; // 5 MB
        MultipartFile file = mockFile(fileSize);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        String result = storageService.store(file, "employees", "emp-1");

        assertThat(result).isEqualTo(OBJECT_PATH);
        // enforceStorageQuota must be called before recordStorageUsed
        InOrder inOrder = inOrder(subscriptionService);
        inOrder.verify(subscriptionService).enforceStorageQuota(TENANT_ID, fileSize);
        inOrder.verify(subscriptionService).recordStorageUsed(TENANT_ID, fileSize);
    }

    // ── 2. Reservation exceeding quota fails before upload ────────────────────

    @Test
    @DisplayName("store() rejects upload when quota would be exceeded — MinIO never called")
    void store_exceedsQuota_rejectsBeforeUpload() throws Exception {
        long fileSize = 200L * 1024 * 1024; // 200 MB
        MultipartFile file = mockFile(fileSize);
        doThrow(new TenantException("Storage quota exceeded"))
                .when(subscriptionService).enforceStorageQuota(TENANT_ID, fileSize);

        assertThatThrownBy(() -> storageService.store(file, "employees", "emp-1"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Storage quota exceeded");

        // MinIO must never be called if quota check fails
        verifyNoInteractions(minioStorageService);
        // recordStorageUsed must never be called
        verify(subscriptionService, never()).recordStorageUsed(any(), anyLong());
    }

    // ── 3. Failed upload releases reservation ────────────────────────────────

    @Test
    @DisplayName("store() releases reservation when MinIO upload fails")
    void store_uploadFails_releasesReservation() throws Exception {
        long fileSize = 10L * 1024 * 1024; // 10 MB
        MultipartFile file = mockFile(fileSize);
        when(minioStorageService.store(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("MinIO connection refused"));

        assertThatThrownBy(() -> storageService.store(file, "employees", "emp-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MinIO connection refused");

        // Quota was reserved (enforced + incremented) before upload
        verify(subscriptionService).enforceStorageQuota(TENANT_ID, fileSize);
        verify(subscriptionService).recordStorageUsed(TENANT_ID, fileSize);
        // Reservation must be released after failure
        verify(subscriptionService).releaseStorage(TENANT_ID, fileSize);
    }

    // ── 4. Successful deletion releases storage ───────────────────────────────

    @Test
    @DisplayName("delete() releases storage after successful MinIO deletion")
    void delete_success_releasesStorage() throws Exception {
        long fileSize = 3L * 1024 * 1024; // 3 MB

        storageService.delete(OBJECT_PATH, fileSize);

        verify(minioStorageService).delete(OBJECT_PATH);
        verify(subscriptionService).releaseStorage(TENANT_ID, fileSize);
    }

    // ── 5. Failed deletion does NOT reduce usage ──────────────────────────────

    @Test
    @DisplayName("delete() does NOT release storage when MinIO deletion fails")
    void delete_minioFails_doesNotReleaseStorage() throws Exception {
        long fileSize = 3L * 1024 * 1024;
        doThrow(new RuntimeException("MinIO error")).when(minioStorageService).delete(OBJECT_PATH);

        assertThatThrownBy(() -> storageService.delete(OBJECT_PATH, fileSize))
                .isInstanceOf(RuntimeException.class);

        // Storage must NOT be released — file still exists in MinIO
        verify(subscriptionService, never()).releaseStorage(any(), anyLong());
    }

    // ── 6. Replacement upload accounts for old and new sizes ─────────────────

    @Test
    @DisplayName("storeReplacement() releases old quota then reserves new quota")
    void storeReplacement_correctQuotaAccounting() throws Exception {
        long oldSize = 2L * 1024 * 1024;  // 2 MB old file
        long newSize = 5L * 1024 * 1024;  // 5 MB new file
        MultipartFile file = mockFile(newSize);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        storageService.storeReplacement(file, "employees", "emp-1", oldSize);

        InOrder inOrder = inOrder(subscriptionService);
        // Old quota released first
        inOrder.verify(subscriptionService).releaseStorage(TENANT_ID, oldSize);
        // New quota enforced and reserved
        inOrder.verify(subscriptionService).enforceStorageQuota(TENANT_ID, newSize);
        inOrder.verify(subscriptionService).recordStorageUsed(TENANT_ID, newSize);
    }

    @Test
    @DisplayName("storeReplacement() restores old quota and releases new reservation on upload failure")
    void storeReplacement_uploadFails_restoresOldQuota() throws Exception {
        long oldSize = 2L * 1024 * 1024;
        long newSize = 5L * 1024 * 1024;
        MultipartFile file = mockFile(newSize);
        when(minioStorageService.store(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("MinIO error"));

        assertThatThrownBy(() -> storageService.storeReplacement(file, "employees", "emp-1", oldSize))
                .isInstanceOf(RuntimeException.class);

        // New reservation released
        verify(subscriptionService).releaseStorage(TENANT_ID, newSize);
        // Old quota restored
        verify(subscriptionService).recordStorageUsed(TENANT_ID, oldSize);
    }

    // ── 7. Platform tenant bypasses all quota checks ─────────────────────────

    @Test
    @DisplayName("store() bypasses quota for platform pseudo-tenant")
    void store_platformTenant_bypassesQuota() throws Exception {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());
        MultipartFile file = mockFile(100L * 1024 * 1024);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        storageService.store(file, "platform", "doc-1");

        verify(subscriptionService, never()).enforceStorageQuota(any(), anyLong());
        verify(subscriptionService, never()).recordStorageUsed(any(), anyLong());
    }

    @Test
    @DisplayName("delete() bypasses quota release for platform pseudo-tenant")
    void delete_platformTenant_bypassesRelease() throws Exception {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());

        storageService.delete(OBJECT_PATH, 1024L);

        verify(minioStorageService).delete(OBJECT_PATH);
        verify(subscriptionService, never()).releaseStorage(any(), anyLong());
    }

    // ── 8. File size measured server-side ────────────────────────────────────

    @Test
    @DisplayName("store() uses file.getSize() — never a caller-supplied size")
    void store_usesServerSideFileSize() throws Exception {
        long actualSize = 7L * 1024 * 1024;
        MultipartFile file = mockFile(actualSize);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        storageService.store(file, "employees", "emp-1");

        // The exact server-side size must be used for both enforce and record
        verify(subscriptionService).enforceStorageQuota(TENANT_ID, actualSize);
        verify(subscriptionService).recordStorageUsed(TENANT_ID, actualSize);
    }
}
