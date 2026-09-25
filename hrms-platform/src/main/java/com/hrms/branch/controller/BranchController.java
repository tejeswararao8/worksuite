package com.hrms.branch.controller;

import com.hrms.branch.dto.BranchDto;
import com.hrms.branch.service.BranchService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Branch Management", description = "Manage company branches/offices. " +
        "Each branch belongs to a company and can be assigned to employees as their work location. " +
        "Branch codes must be unique within a company.")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create branch",
            description = "Creates a new branch/office for the authenticated company. " +
                    "Branch code must be unique within the company (e.g. DXB-HQ, AUH-01). " +
                    "Only one branch can be marked as head office."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Branch created successfully"),
            @ApiResponse(responseCode = "409", description = "Branch with the same code already exists in this company"),
            @ApiResponse(responseCode = "400", description = "Validation error — name or code missing"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<BranchDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Branch details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Dubai Head Office",
                                      "code": "DXB-HQ",
                                      "address": "Level 10, Business Bay Tower, Dubai",
                                      "city": "Dubai",
                                      "country": "UAE",
                                      "phone": "+97142345678",
                                      "email": "dubai@company.com",
                                      "headOffice": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody BranchDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created", branchService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get branch by ID",
            description = "Returns details of a specific branch. " +
                    "Any authenticated user within the company can view branch details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch details returned"),
            @ApiResponse(responseCode = "404", description = "Branch not found or belongs to a different company"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<BranchDto.Response>> findById(
            @Parameter(description = "Branch UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all branches",
            description = "Returns a paginated list of all active branches for the authenticated company, sorted alphabetically by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of branches"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<BranchDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                branchService.findAll(PageRequest.of(page, size, Sort.by("name")))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Update branch",
            description = "Updates branch information. Only provided fields are updated. " +
                    "Branch code cannot be changed if employees are assigned to this branch."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch updated successfully"),
            @ApiResponse(responseCode = "404", description = "Branch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<BranchDto.Response>> update(
            @Parameter(description = "Branch UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody BranchDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete branch",
            description = "Soft-deletes a branch. The branch record is retained for historical data. " +
                    "Employees currently assigned to this branch should be transferred before deletion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Branch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Branch UUID", required = true) @PathVariable UUID id) {
        branchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted", null));
    }
}
