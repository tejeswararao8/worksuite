package com.platform.tenancy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantSecurityService")
class TenantSecurityServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantSecurityService tenantSecurityService;

    private static final UUID ACTIVE_TENANT_ID   = UUID.randomUUID();
    private static final UUID SUSPENDED_TENANT_ID = UUID.randomUUID();
    private static final UUID CANCELLED_TENANT_ID = UUID.randomUUID();
    private static final UUID INACTIVE_TENANT_ID  = UUID.randomUUID();
    private static final UUID UNKNOWN_TENANT_ID   = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Tenant active = Tenant.builder()
                .status(Tenant.TenantStatus.ACTIVE)
                .isActive(true)
                .build();

        Tenant suspended = Tenant.builder()
                .status(Tenant.TenantStatus.SUSPENDED)
                .isActive(true)
                .build();

        Tenant cancelled = Tenant.builder()
                .status(Tenant.TenantStatus.CANCELLED)
                .isActive(true)
                .build();

        Tenant inactive = Tenant.builder()
                .status(Tenant.TenantStatus.ACTIVE)
                .isActive(false)
                .build();

        lenient().when(tenantRepository.findById(ACTIVE_TENANT_ID))
                .thenReturn(Optional.of(active));
        lenient().when(tenantRepository.findById(SUSPENDED_TENANT_ID))
                .thenReturn(Optional.of(suspended));
        lenient().when(tenantRepository.findById(CANCELLED_TENANT_ID))
                .thenReturn(Optional.of(cancelled));
        lenient().when(tenantRepository.findById(INACTIVE_TENANT_ID))
                .thenReturn(Optional.of(inactive));
        lenient().when(tenantRepository.findById(UNKNOWN_TENANT_ID))
                .thenReturn(Optional.empty());
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Active tenant passes validation")
    void activeTenant_passes() {
        assertThatNoException()
                .isThrownBy(() -> tenantSecurityService.validateActiveTenant(
                        ACTIVE_TENANT_ID.toString()));
    }

    @Test
    @DisplayName("Platform pseudo-tenant bypasses DB check")
    void platformTenant_bypassesDbCheck() {
        // Platform tenant ID must never hit the repository
        assertThatNoException()
                .isThrownBy(() -> tenantSecurityService.validateActiveTenant(
                        TenantSecurityService.PLATFORM_TENANT_ID.toString()));

        verify(tenantRepository, never()).findById(TenantSecurityService.PLATFORM_TENANT_ID);
    }

    // ── Rejection cases ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Null tenantId is rejected")
    void nullTenantId_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant(null))
                .isInstanceOf(TenantSecurityException.class);
    }

    @Test
    @DisplayName("Blank tenantId is rejected")
    void blankTenantId_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant("   "))
                .isInstanceOf(TenantSecurityException.class);
    }

    @Test
    @DisplayName("Malformed UUID is rejected")
    void malformedUuid_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant("not-a-uuid"))
                .isInstanceOf(TenantSecurityException.class);
    }

    @Test
    @DisplayName("Unknown tenant is rejected")
    void unknownTenant_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant(
                UNKNOWN_TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Suspended tenant is rejected with 403")
    void suspendedTenant_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant(
                SUSPENDED_TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    @DisplayName("Cancelled tenant is rejected")
    void cancelledTenant_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant(
                CANCELLED_TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("Inactive tenant (isActive=false) is rejected")
    void inactiveTenant_rejected() {
        assertThatThrownBy(() -> tenantSecurityService.validateActiveTenant(
                INACTIVE_TENANT_ID.toString()))
                .isInstanceOf(TenantSecurityException.class)
                .hasMessageContaining("inactive");
    }

    // ── isPlatformTenant helper ───────────────────────────────────────────────

    @Test
    @DisplayName("isPlatformTenant returns true for platform UUID")
    void isPlatformTenant_trueForPlatformId() {
        assertThat(TenantSecurityService.isPlatformTenant(
                TenantSecurityService.PLATFORM_TENANT_ID.toString())).isTrue();
    }

    @Test
    @DisplayName("isPlatformTenant returns false for regular tenant")
    void isPlatformTenant_falseForRegularTenant() {
        assertThat(TenantSecurityService.isPlatformTenant(
                ACTIVE_TENANT_ID.toString())).isFalse();
    }

    @Test
    @DisplayName("isPlatformTenant returns false for null")
    void isPlatformTenant_falseForNull() {
        assertThat(TenantSecurityService.isPlatformTenant(null)).isFalse();
    }
}
