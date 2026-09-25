package com.hrms.audit.controller;

import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.repository.AuditLogRepository;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit Logs", description = "Immutable audit trail for all system actions. " +
        "Every create, update, delete, login, logout, export, import, approve, and reject action is logged. " +
        "Audit logs store: entity name, entity ID, action, old value (JSON), new value (JSON), user, timestamp, and IP address. " +
        "Logs are immutable — they cannot be modified or deleted. Only COMPANY_ADMIN can view audit logs.")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Get all audit logs",
            description = "Returns a paginated list of all audit logs for the company, sorted by timestamp descending. " +
                    "Use this to track all system activity and changes made by users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of audit logs"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(auditLogRepository.findByCompanyIdOrderByPerformedAtDesc(
                        companyId, PageRequest.of(page, size, Sort.by("performedAt").descending())))));
    }

    @GetMapping("/entity/{entityName}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Get audit logs by entity type",
            description = "Returns audit logs filtered by entity type (e.g. Employee, Department, Asset). " +
                    "Use this to see all changes made to a specific type of record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered audit logs returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> findByEntity(
            @Parameter(description = "Entity type name (e.g. Employee, Department, Asset)", required = true, example = "Employee")
            @PathVariable String entityName,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(auditLogRepository.findByCompanyIdAndEntityNameOrderByPerformedAtDesc(
                        companyId, entityName, PageRequest.of(page, size)))));
    }

    @GetMapping("/record/{entityId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Get audit logs for a specific record",
            description = "Returns the complete change history for a specific record by its UUID. " +
                    "Use this to see all changes made to a specific employee, asset, document, etc."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Change history for the record returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> findByRecord(
            @Parameter(description = "Entity record UUID (e.g. employee ID, asset ID)", required = true)
            @PathVariable String entityId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(auditLogRepository.findByCompanyIdAndEntityIdOrderByPerformedAtDesc(
                        companyId, entityId, PageRequest.of(page, size)))));
    }
}
