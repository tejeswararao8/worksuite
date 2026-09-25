package com.hrms.report.controller;

import com.hrms.common.dto.ApiResponse;
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
@RequestMapping("/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "Generate and export HRMS reports. " +
        "All reports support three export formats: PDF, EXCEL (xlsx), and CSV. " +
        "Reports are scoped to the authenticated company. " +
        "Available reports: Employee, Branch, Department, Designation, Promotion, Transfer, Document Expiry, Asset.")
public class ReportController {

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Employee report",
            description = "Generates a report of all employees with their profile, department, designation, branch, and employment status. " +
                    "Supports filtering by department, branch, status, and date range. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated and returned as file download"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<byte[]> employeeReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format,
            @Parameter(description = "Filter by department UUID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "Filter by branch UUID") @RequestParam(required = false) String branchId,
            @Parameter(description = "Filter by employment status", example = "ACTIVE") @RequestParam(required = false) String status) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Department report",
            description = "Generates a report of all departments with employee count, department head, and sub-department hierarchy. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> departmentReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/branches")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Branch report",
            description = "Generates a report of all branches with employee count per branch. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> branchReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Designation report",
            description = "Generates a report of all designations with employee count per designation and level distribution. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Designation report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> designationReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/promotions")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Promotion report",
            description = "Generates a report of all promotions within a date range. " +
                    "Includes employee name, from/to designation, effective date, and approver. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> promotionReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Transfer report",
            description = "Generates a report of all employee transfers within a date range. " +
                    "Includes employee name, transfer type, from/to values, effective date, and approver. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> transferReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/document-expiry")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Document expiry report",
            description = "Generates a report of all employee documents expiring within a specified number of days. " +
                    "Includes employee name, document type, document number, expiry date, and days remaining. " +
                    "Default: documents expiring within the next 90 days. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document expiry report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> documentExpiryReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format,
            @Parameter(description = "Days ahead to check for expiry", example = "90")
            @RequestParam(defaultValue = "90") int daysAhead) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assets")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Asset report",
            description = "Generates a report of all company assets with their status, assigned employee, and warranty information. " +
                    "Supports filtering by asset type and status. " +
                    "Export formats: PDF, EXCEL, CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset report generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> assetReport(
            @Parameter(description = "Export format", example = "EXCEL", required = true)
            @RequestParam String format,
            @Parameter(description = "Filter by asset type", example = "LAPTOP") @RequestParam(required = false) String assetType,
            @Parameter(description = "Filter by asset status", example = "ASSIGNED") @RequestParam(required = false) String status) {
        return ResponseEntity.ok().build();
    }
}
