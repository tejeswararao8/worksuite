package com.hrms.company.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.company.dto.CompanyDto;
import com.hrms.company.service.CompanyService;
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
@RequestMapping("/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Company Management", description = "Manage tenant companies on the HRMS platform. " +
        "Only SUPER_ADMIN can create or delete companies. COMPANY_ADMIN can view and update their own company.")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Create a new company",
            description = "Registers a new tenant company on the platform. " +
                    "Each company is an isolated tenant — its data is never visible to other companies. " +
                    "Only SUPER_ADMIN can perform this action. " +
                    "Registration number must be unique across the platform."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "409", description = "Company with the same registration number already exists"),
            @ApiResponse(responseCode = "400", description = "Validation error — required fields missing"),
            @ApiResponse(responseCode = "403", description = "Access denied — SUPER_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<CompanyDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Company details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Acme Corp",
                                      "legalName": "Acme Corporation LLC",
                                      "registrationNumber": "REG-2024-001",
                                      "taxNumber": "TAX-123456",
                                      "email": "info@acme.com",
                                      "phone": "+971501234567",
                                      "website": "https://acme.com",
                                      "address": "123 Business Bay, Dubai",
                                      "country": "UAE",
                                      "city": "Dubai",
                                      "industry": "Technology"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CompanyDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created", companyService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPANY_ADMIN')")
    @Operation(
            summary = "Get company by ID",
            description = "Retrieves full details of a company by its UUID. " +
                    "SUPER_ADMIN can fetch any company. COMPANY_ADMIN can only fetch their own company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company details returned"),
            @ApiResponse(responseCode = "404", description = "Company not found or has been deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<CompanyDto.Response>> findById(
            @Parameter(description = "Company UUID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "List all companies",
            description = "Returns a paginated list of all active companies on the platform. " +
                    "Results are sorted by creation date descending. Only accessible by SUPER_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of companies"),
            @ApiResponse(responseCode = "403", description = "Access denied — SUPER_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page (max 100)", example = "20") @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(companyService.findAll(pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPANY_ADMIN')")
    @Operation(
            summary = "Update company",
            description = "Updates company information. All fields are optional — only provided fields are updated. " +
                    "Registration number cannot be changed once set."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<CompanyDto.Response>> update(
            @Parameter(description = "Company UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody CompanyDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success("Company updated", companyService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Delete company",
            description = "Soft-deletes a company by setting is_active = false. " +
                    "The company and all its data remain in the database but are no longer accessible. " +
                    "This action is irreversible via the API. Only SUPER_ADMIN can delete companies."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — SUPER_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Company UUID", required = true) @PathVariable UUID id) {
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted", null));
    }
}
