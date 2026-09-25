package com.hrms.designation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.designation.dto.DesignationDto;
import com.hrms.designation.service.DesignationService;
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
@RequestMapping("/designations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Designation Management", description = "Manage job designations/titles within the company. " +
        "Designations are used in employee profiles and promotion workflows. " +
        "Each designation can have a level (seniority) and grade for compensation banding.")
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create designation",
            description = "Creates a new job designation/title for the company. " +
                    "Use `level` to define seniority (e.g. 1=Junior, 2=Mid, 3=Senior, 4=Lead, 5=Manager). " +
                    "Use `grade` for compensation banding (e.g. L1, L2, G3)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Designation created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — title is required"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<DesignationDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Designation details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Senior Software Engineer",
                                      "description": "Experienced engineer with 5+ years",
                                      "level": 3,
                                      "grade": "L3"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody DesignationDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Designation created", designationService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get designation by ID",
            description = "Returns details of a specific designation including its level and grade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Designation details returned"),
            @ApiResponse(responseCode = "404", description = "Designation not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<DesignationDto.Response>> findById(
            @Parameter(description = "Designation UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(designationService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all designations",
            description = "Returns a paginated list of all active designations for the company, sorted alphabetically by title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of designations"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<DesignationDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                designationService.findAll(PageRequest.of(page, size, Sort.by("title")))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Update designation",
            description = "Updates designation details. Changes to level or grade will reflect on all employees holding this designation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Designation updated successfully"),
            @ApiResponse(responseCode = "404", description = "Designation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<DesignationDto.Response>> update(
            @Parameter(description = "Designation UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody DesignationDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success("Designation updated", designationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete designation",
            description = "Soft-deletes a designation. Employees currently holding this designation are not affected — " +
                    "their designation reference is retained for historical accuracy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Designation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Designation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Designation UUID", required = true) @PathVariable UUID id) {
        designationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deleted", null));
    }
}
