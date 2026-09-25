package com.hrms.promotion.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.promotion.dto.PromotionDto;
import com.hrms.promotion.service.PromotionService;
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
@RequestMapping("/promotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Promotion Management", description = "Manage employee promotions. " +
        "A promotion changes an employee's designation to a higher-level one. " +
        "All promotions require approval and maintain a complete history. " +
        "Upon approval, the employee's designation is automatically updated on their profile.")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Initiate promotion request",
            description = "Creates a promotion request for an employee. The request starts in PENDING status and requires approval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promotion request created and pending approval"),
            @ApiResponse(responseCode = "404", description = "Employee or target designation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<PromotionDto.Response>> initiate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Promotion request details", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "toDesignationId": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
                                      "effectiveDate": "2024-04-01",
                                      "reason": "Exceptional performance in Q1 2024"
                                    }
                                    """))
            )
            @RequestBody PromotionDto.InitiateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promotion request created", promotionService.initiate(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "List all promotions",
            description = "Returns a paginated list of all promotion requests for the company, sorted by creation date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of promotions"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PagedResponse<PromotionDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get promotion history for employee",
            description = "Returns the complete promotion history for an employee, sorted by effective date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion history returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<PagedResponse<PromotionDto.Response>>> getHistory(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.getHistory(employeeId, PageRequest.of(page, size, Sort.by("effectiveDate").descending()))));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN')")
    @Operation(
            summary = "Approve promotion",
            description = "Approves a pending promotion. Upon approval, the employee's designation is automatically updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion approved — employee designation updated"),
            @ApiResponse(responseCode = "404", description = "Promotion request not found"),
            @ApiResponse(responseCode = "422", description = "Promotion is not in PENDING status"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<PromotionDto.Response>> approve(
            @Parameter(description = "Promotion UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Promotion approved", promotionService.approve(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN')")
    @Operation(
            summary = "Reject promotion",
            description = "Rejects a pending promotion request. The employee's designation remains unchanged."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion rejected"),
            @ApiResponse(responseCode = "404", description = "Promotion request not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<PromotionDto.Response>> reject(
            @Parameter(description = "Promotion UUID", required = true) @PathVariable UUID id,
            @RequestBody PromotionDto.ActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promotion rejected", promotionService.reject(id, request)));
    }
}
