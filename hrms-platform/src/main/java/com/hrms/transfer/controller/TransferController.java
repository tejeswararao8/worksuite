package com.hrms.transfer.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.transfer.dto.TransferDto;
import com.hrms.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transfer Management", description = "Manage employee transfers across departments, branches, teams, and managers. " +
        "All transfers require approval and maintain a complete history. " +
        "Transfer types: DEPARTMENT, BRANCH, MANAGER, TEAM. " +
        "Upon approval, the employee's profile is automatically updated.")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Initiate transfer request",
            description = "Creates a transfer request for an employee. The request starts in PENDING status and requires approval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer request created and pending approval"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<TransferDto.Response>> initiate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transfer request details", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "transferType": "DEPARTMENT",
                                      "toDepartmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa2",
                                      "effectiveDate": "2024-02-01",
                                      "reason": "Business requirement"
                                    }
                                    """))
            )
            @RequestBody TransferDto.InitiateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer request created", transferService.initiate(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "List all transfers",
            description = "Returns a paginated list of all transfer requests for the company, sorted by creation date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of transfers"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<TransferDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                transferService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get transfer history for employee",
            description = "Returns the complete transfer history for an employee, sorted by effective date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer history returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<PagedResponse<TransferDto.Response>>> getHistory(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                transferService.getHistory(employeeId, PageRequest.of(page, size, Sort.by("effectiveDate").descending()))));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Approve transfer request",
            description = "Approves a pending transfer. Upon approval, the employee's profile is automatically updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer approved — employee profile updated"),
            @ApiResponse(responseCode = "404", description = "Transfer request not found"),
            @ApiResponse(responseCode = "422", description = "Transfer is not in PENDING status"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<TransferDto.Response>> approve(
            @Parameter(description = "Transfer UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Transfer approved", transferService.approve(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Reject transfer request",
            description = "Rejects a pending transfer request. The employee profile remains unchanged."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer rejected"),
            @ApiResponse(responseCode = "404", description = "Transfer request not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<TransferDto.Response>> reject(
            @Parameter(description = "Transfer UUID", required = true) @PathVariable UUID id,
            @RequestBody TransferDto.ActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer rejected", transferService.reject(id, request)));
    }
}
