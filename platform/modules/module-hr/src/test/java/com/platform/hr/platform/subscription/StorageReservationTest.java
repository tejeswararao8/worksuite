package com.platform.hr.platform.subscription;

import com.platform.document.MinioStorageService;
import com.platform.hr.platform.subscription.SubscriptionService;
import com.platform.hr.subscription.QuotaAwareStorageService;
import com.platform.hr.subscription.StorageReservationService;
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
 * Unit tests for QuotaAwareStorageService and StorageReservationService.
 *
 * <p>Key fix verified: {@code reserveStorage()} is now delegated to
 * {@link StorageReservationService} — a separate Spring bean — so the
 * {@code @Transactional(REQUIRES_NEW)} proxy is honoured. Self-invocation
 * within {@code QuotaAwareStorageService} would bypass the proxy entirely.
 *
 * <p>Covers:
 * <ul>
 *   <li>Reservation within quota: enforce → record order enforced</li>
 *   <li>Quota exceeded: MinIO never called</li>
 *   <li>Upload failure: reservation released via StorageReservationService</li>
 *   <li>Successful deletion releases storage</li>
 *   <li>Failed deletion does NOT release storage</li>
 *   <li>Replacement: release old → reserve new → upload</li>
 *   <li>Replacement upload failure: new reservation released, old quota restored</li>
 *   <li>Platform tenant bypasses all quota checks</li>
 *   <li>StorageReservationService.reserve() calls enforce then record in order</li>
 *   <li>StorageReservationService.release() swallows exceptions and logs</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuotaAwareStorageService + StorageReservationService — Storage Reservation Tests")
class StorageReservationTest {

    @Mock private MinioStorageService minioStorageService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private StorageReservationService reservationService;

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
    @DisplayName("store() calls reservationService.reserve() before MinIO upload")
    void store_withinQuota_reservesBeforeUpload() throws Exception {
        long fileSize = 5L * 1024 * 1024;
        MultipartFile file = mockFile(fileSize);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        String result = storageService.store(file, "employees", "emp-1");

        assertThat(result).isEqualTo(OBJECT_PATH);
        // Reservation must happen before MinIO upload
        InOrder inOrder = inOrder(reservationService, minioStorageService);
        inOrder.verify(reservationService).reserve(TENANT_ID, fileSize);
        inOrder.verify(minioStorageService).store(any(), any(), any(), any());
    }

    // ── 2. Quota exceeded — MinIO never called ────────────────────────────────

    @Test
    @DisplayName("store() rejects upload when quota would be exceeded — MinIO never called")
    void store_exceedsQuota_rejectsBeforeUpload() throws Exception {
        long fileSize = 200L * 1024 * 1024;
        MultipartFile file = mockFile(fileSize);
        doThrow(new TenantException("Storage quota exceeded"))
                .when(reservationService).reserve(TENANT_ID, fileSize);

        assertThatThrownBy(() -> storageService.store(file, "employees", "emp-1"))
                .isInstanceOf(TenantException.class)
                .hasMessageContaining("Storage quota exceeded");

        verifyNoInteractions(minioStorageService);
    }

    // ── 3. Upload failure releases reservation ────────────────────────────────

    @Test
    @DisplayName("store() calls reservationService.release() when MinIO upload fails")
    void store_uploadFails_releasesReservation() throws Exception {
        long fileSize = 10L * 1024 * 1024;
        MultipartFile file = mockFile(fileSize);
        when(minioStorageService.store(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("MinIO connection refused"));

        assertThatThrownBy(() -> storageService.store(file, "employees", "emp-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MinIO connection refused");

        verify(reservationService).reserve(TENANT_ID, fileSize);
        verify(reservationService).release(TENANT_ID, fileSize);
    }

    // ── 4. Successful deletion releases storage ───────────────────────────────

    @Test
    @DisplayName("delete() releases storage after successful MinIO deletion")
    void delete_success_releasesStorage() throws Exception {
        long fileSize = 3L * 1024 * 1024;

        storageService.delete(OBJECT_PATH, fileSize);

        verify(minioStorageService).delete(OBJECT_PATH);
        verify(subscriptionService).releaseStorage(TENANT_ID, fileSize);
    }

    // ── 5. Failed deletion does NOT release storage ───────────────────────────

    @Test
    @DisplayName("delete() does NOT release storage when MinIO deletion fails")
    void delete_minioFails_doesNotReleaseStorage() throws Exception {
        long fileSize = 3L * 1024 * 1024;
        doThrow(new RuntimeException("MinIO error")).when(minioStorageService).delete(OBJECT_PATH);

        assertThatThrownBy(() -> storageService.delete(OBJECT_PATH, fileSize))
                .isInstanceOf(RuntimeException.class);

        verify(subscriptionService, never()).releaseStorage(any(), anyLong());
    }

    // ── 6. Replacement: release old → reserve new → upload ───────────────────

    @Test
    @DisplayName("storeReplacement() releases old quota then reserves new quota before upload")
    void storeReplacement_correctQuotaAccounting() throws Exception {
        long oldSize = 2L * 1024 * 1024;
        long newSize = 5L * 1024 * 1024;
        MultipartFile file = mockFile(newSize);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        storageService.storeReplacement(file, "employees", "emp-1", oldSize);

        InOrder inOrder = inOrder(subscriptionService, reservationService, minioStorageService);
        inOrder.verify(subscriptionService).releaseStorage(TENANT_ID, oldSize);
        inOrder.verify(reservationService).reserve(TENANT_ID, newSize);
        inOrder.verify(minioStorageService).store(any(), any(), any(), any());
    }

    // ── 7. Replacement upload failure: rollback ───────────────────────────────

    @Test
    @DisplayName("storeReplacement() releases new reservation and restores old quota on upload failure")
    void storeReplacement_uploadFails_rollsBack() throws Exception {
        long oldSize = 2L * 1024 * 1024;
        long newSize = 5L * 1024 * 1024;
        MultipartFile file = mockFile(newSize);
        when(minioStorageService.store(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("MinIO error"));

        assertThatThrownBy(() -> storageService.storeReplacement(file, "employees", "emp-1", oldSize))
                .isInstanceOf(RuntimeException.class);

        verify(reservationService).release(TENANT_ID, newSize);
        verify(subscriptionService).recordStorageUsed(TENANT_ID, oldSize);
    }

    // ── 8. Platform tenant bypasses all quota checks ─────────────────────────

    @Test
    @DisplayName("store() bypasses quota for platform pseudo-tenant")
    void store_platformTenant_bypassesQuota() throws Exception {
        TenantContext.setTenantId(TenantSecurityService.PLATFORM_TENANT_ID.toString());
        MultipartFile file = mockFile(100L * 1024 * 1024);
        when(minioStorageService.store(any(), any(), any(), any())).thenReturn(OBJECT_PATH);

        storageService.store(file, "platform", "doc-1");

        verifyNoInteractions(reservationService);
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

    // ── 9. StorageReservationService.reserve() calls enforce then record ──────

    @Test
    @DisplayName("StorageReservationService.reserve() calls enforceStorageQuota then recordStorageUsed in order")
    void reservationService_reserve_callsEnforceThenRecord() {
        StorageReservationService svc = new StorageReservationService(subscriptionService);
        long fileSize = 5L * 1024 * 1024;

        svc.reserve(TENANT_ID, fileSize);

        InOrder inOrder = inOrder(subscriptionService);
        inOrder.verify(subscriptionService).enforceStorageQuota(TENANT_ID, fileSize);
        inOrder.verify(subscriptionService).recordStorageUsed(TENANT_ID, fileSize);
    }

    // ── 10. StorageReservationService.release() swallows exceptions ───────────

    @Test
    @DisplayName("StorageReservationService.release() does not throw when releaseStorage fails")
    void reservationService_release_swallowsException() {
        StorageReservationService svc = new StorageReservationService(subscriptionService);
        doThrow(new RuntimeException("DB error")).when(subscriptionService).releaseStorage(any(), anyLong());

        assertThatNoException().isThrownBy(() -> svc.release(TENANT_ID, 1024L));
    }
}
