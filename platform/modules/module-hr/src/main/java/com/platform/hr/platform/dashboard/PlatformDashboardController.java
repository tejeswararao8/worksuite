package com.platform.hr.platform.dashboard;

import com.platform.api.ApiResponse;
import com.platform.security.rbac.PlatformRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform admin dashboard APIs.
 *
 * All endpoints require a platform role — tenant users are denied at the
 * method-security level before any data is accessed.
 *
 * No X-Tenant-ID header is required or used — platform users operate
 * cross-tenant via the platform pseudo-tenant (00000000-0000-0000-0000-000000000001).
 */
@RestController
@RequestMapping("/api/v1/platform/dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {

    private final PlatformDashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('" +
            PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "', '" +
            PlatformRoles.PLATFORM_SUPPORT + "', '" +
            PlatformRoles.PLATFORM_IMPLEMENTATION + "')")
    public ResponseEntity<ApiResponse<PlatformDashboardDto>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }
}
