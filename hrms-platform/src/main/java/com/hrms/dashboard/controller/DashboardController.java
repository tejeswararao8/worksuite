package com.hrms.dashboard.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Dashboard summary statistics and KPIs for the HRMS platform. " +
        "Returns aggregated counts and metrics for the authenticated company. " +
        "Data is cached in Redis and refreshed every 5 minutes.")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns key HR metrics: total employees, active employees, employees on probation, " +
                    "documents expiring in 30 days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard summary returned"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<DashboardService.Summary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/headcount-by-department")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Headcount by department",
            description = "Returns employee count grouped by department. Used to render bar/pie charts on the dashboard."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Headcount by department returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Object>> headcountByDepartment() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getHeadcountByDepartment()));
    }

    @GetMapping("/headcount-by-branch")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Headcount by branch",
            description = "Returns employee count grouped by branch/office location."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Headcount by branch returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Object>> headcountByBranch() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getHeadcountByBranch()));
    }

    @GetMapping("/joining-trend")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Employee joining trend",
            description = "Returns monthly employee joining counts for the last 12 months."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly joining trend data returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Object>> joiningTrend(
            @Parameter(description = "Number of months to look back", example = "12")
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getJoiningTrend(months)));
    }

    @GetMapping("/document-expiry-alerts")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Document expiry alerts",
            description = "Returns count of documents expiring in the next 30 days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document expiry alert summary returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Object>> documentExpiryAlerts() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDocumentExpiryAlerts()));
    }

    @GetMapping("/asset-utilization")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Asset utilization summary",
            description = "Returns asset counts grouped by status (AVAILABLE, ASSIGNED, IN_REPAIR, RETIRED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset utilization summary returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Object>> assetUtilization() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAssetUtilization()));
    }
}
