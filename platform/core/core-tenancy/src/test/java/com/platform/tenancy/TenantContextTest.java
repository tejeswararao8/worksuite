package com.platform.tenancy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TenantContext")
class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("setTenantId and getTenantId round-trip")
    void setAndGet() {
        String id = UUID.randomUUID().toString();
        TenantContext.setTenantId(id);
        assertThat(TenantContext.getTenantId()).isEqualTo(id);
    }

    @Test
    @DisplayName("hasTenant returns false when not set")
    void hasTenant_falseWhenEmpty() {
        assertThat(TenantContext.hasTenant()).isFalse();
    }

    @Test
    @DisplayName("hasTenant returns true after set")
    void hasTenant_trueAfterSet() {
        TenantContext.setTenantId(UUID.randomUUID().toString());
        assertThat(TenantContext.hasTenant()).isTrue();
    }

    @Test
    @DisplayName("clear removes tenant ID")
    void clear_removesTenantId() {
        TenantContext.setTenantId(UUID.randomUUID().toString());
        TenantContext.clear();
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.hasTenant()).isFalse();
    }

    @Test
    @DisplayName("ThreadLocal isolation — different threads see different values")
    void threadLocalIsolation() throws InterruptedException {
        String mainThreadId = UUID.randomUUID().toString();
        TenantContext.setTenantId(mainThreadId);

        AtomicReference<String> otherThreadValue = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread other = new Thread(() -> {
            // Other thread should see null — not the main thread's value
            otherThreadValue.set(TenantContext.getTenantId());
            latch.countDown();
        });
        other.start();
        latch.await();

        assertThat(otherThreadValue.get())
                .as("Other thread must not see main thread's tenant context")
                .isNull();

        // Main thread value must be unchanged
        assertThat(TenantContext.getTenantId()).isEqualTo(mainThreadId);
    }

    @Test
    @DisplayName("clear in finally block prevents leakage across requests")
    void clearInFinally_preventsLeakage() {
        String tenantId = UUID.randomUUID().toString();
        try {
            TenantContext.setTenantId(tenantId);
            assertThat(TenantContext.hasTenant()).isTrue();
        } finally {
            TenantContext.clear();
        }
        assertThat(TenantContext.hasTenant())
                .as("TenantContext must be empty after clear() in finally")
                .isFalse();
    }
}
