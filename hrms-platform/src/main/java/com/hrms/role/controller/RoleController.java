package com.hrms.role.controller;

import com.hrms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Role & Permission Management", description = "Manage RBAC roles and permissions. " +
        "Default roles: SUPER_ADMIN, COMPANY_ADMIN, HR, MANAGER, EMPLOYEE, HELPDESK_AGENT. " +
        "Custom roles can be created per company. " +
        "Permissions: EMPLOYEE_CREATE, EMPLOYEE_VIEW, EMPLOYEE_UPDATE, EMPLOYEE_DELETE, EMPLOYEE_EXPORT, " +
        "DOCUMENT_CREATE, DOCUMENT_VIEW, DOCUMENT_DELETE, ASSET_CREATE, ASSET_VIEW, ASSET_UPDATE, REPORT_EXPORT, AUDIT_VIEW.")
public class RoleController {

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(
            summary = "Create custom role",
            description = "Creates a custom role for the company with specific permissions. " +
                    "Use this to create roles beyond the default set (e.g. PAYROLL_ADMIN, IT_ADMIN). " +
                    "Assign permissions from the available permission list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "409", description = "Role with the same name already exists in this company"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<Void>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role details with permissions",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "PAYROLL_ADMIN",
                                      "description": "Manages payroll and compensation",
                                      "permissionIds": [
                                        "3fa85f64-5717-4562-b3fc-2c963f66afa1",
                                        "3fa85f64-5717-4562-b3fc-2c963f66afa2"
                                      ]
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created", null));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "List all roles",
            description = "Returns all roles available in the company including default and custom roles, " +
                    "along with their assigned permissions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of roles with permissions"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Get role by ID",
            description = "Returns details of a specific role including all assigned permissions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role details returned"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<Void>> findById(
            @Parameter(description = "Role UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(
            summary = "Update role permissions",
            description = "Replaces the full set of permissions for a role. " +
                    "Provide the complete list of permission IDs — existing permissions not in the list will be removed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role permissions updated"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<Void>> updatePermissions(
            @Parameter(description = "Role UUID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New permission IDs",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "permissionIds": [
                                        "3fa85f64-5717-4562-b3fc-2c963f66afa1",
                                        "3fa85f64-5717-4562-b3fc-2c963f66afa2"
                                      ]
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Permissions updated", null));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "List all available permissions",
            description = "Returns all system permissions that can be assigned to roles, grouped by module."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all permissions grouped by module"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> listPermissions() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
