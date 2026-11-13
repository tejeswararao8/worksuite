package com.platform.hr.platform.dashboard;

import lombok.Builder;
import lombok.Getter;

/**
 * KPI summary returned by GET /api/v1/platform/dashboard/summary.
 * All counts are cross-tenant — only accessible to platform roles.
 */
@Getter
@Builder
public class PlatformDashboardDto {

    private long totalTenants;
    private long activeTenants;
    private long pendingApprovals;
    private long suspendedTenants;
    private long totalPlatformEmployees;
    private long openSupportTickets;
}
