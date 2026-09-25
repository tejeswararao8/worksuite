package com.hrms.user.controller;

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
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "Manage system user accounts. " +
        "Each employee can have one user account for system login. " +
        "Users are assigned a role (SUPER_ADMIN, COMPANY_ADMIN, HR, MANAGER, EMPLOYEE). " +
        "User accounts can be activated, deactivated, and have their roles changed.")
public class UserController {

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create user account",
            description = "Creates a system login account for an employee. " +
                    "The email must match the employee's email. " +
                    "A temporary password is set and the user is forced to change it on first login. " +
                    "A welcome email with login credentials is sent to the employee."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User account created — welcome email sent"),
            @ApiResponse(responseCode = "409", description = "User account already exists for this email"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<Void>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User account details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "email": "john.doe@company.com",
                                      "roleId": "3fa85f64-5717-4562-b3fc-2c963f66afa7"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User account created", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Get user by ID",
            description = "Returns user account details including role, last login, and account status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details returned"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> findById(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(
            summary = "Change user role",
            description = "Changes the role assigned to a user account. " +
                    "Only COMPANY_ADMIN can change roles. " +
                    "The change takes effect on the user's next login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    { "roleId": "3fa85f64-5717-4562-b3fc-2c963f66afa7" }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("User role updated", null));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Deactivate user account",
            description = "Deactivates a user account — the user can no longer log in. " +
                    "Existing JWT tokens are invalidated on next request. " +
                    "This is typically done as part of the offboarding process."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account deactivated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User account deactivated", null));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Activate user account",
            description = "Re-activates a previously deactivated user account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account activated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> activate(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User account activated", null));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Reset user password",
            description = "Resets a user's password to a new temporary password. " +
                    "The user is forced to change the password on next login. " +
                    "The new temporary password is sent to the user's email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset — temporary password sent to user's email"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }
}
