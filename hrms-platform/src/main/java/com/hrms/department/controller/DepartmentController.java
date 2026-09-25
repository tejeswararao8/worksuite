package com.hrms.department.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.department.dto.DepartmentDto;
import com.hrms.department.service.DepartmentService;
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
@RequestMapping("/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Department Management", description = "Manage company departments. " +
        "Departments support a parent-child hierarchy (e.g. Engineering → Backend, Frontend). " +
        "Each department can have a designated head employee.")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create department",
            description = "Creates a new department within the authenticated company. " +
                    "Department code must be unique within the company. " +
                    "Set `parentDepartmentId` to create a sub-department (e.g. Backend under Engineering). " +
                    "Set `headEmployeeId` to assign a department head."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "409", description = "Department with the same code already exists"),
            @ApiResponse(responseCode = "400", description = "Validation error — name or code missing"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Department details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Engineering",
                                      "code": "ENG",
                                      "description": "Software Engineering Department",
                                      "parentDepartmentId": null,
                                      "headEmployeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody DepartmentDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created", departmentService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get department by ID",
            description = "Returns details of a specific department including its parent department and head employee references."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department details returned"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> findById(
            @Parameter(description = "Department UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all departments",
            description = "Returns a paginated list of all active departments for the company, sorted alphabetically by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of departments"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<DepartmentDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.findAll(PageRequest.of(page, size, Sort.by("name")))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Update department",
            description = "Updates department details. Only provided fields are updated. " +
                    "You can reassign the department head or change the parent department."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> update(
            @Parameter(description = "Department UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody DepartmentDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", departmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete department",
            description = "Soft-deletes a department. Employees in this department should be transferred first. " +
                    "Sub-departments are not automatically deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Department UUID", required = true) @PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted", null));
    }
}
