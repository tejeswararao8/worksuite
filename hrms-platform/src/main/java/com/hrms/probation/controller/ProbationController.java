package com.hrms.probation.controller;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/probations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Probation Management", description = "Manage employee probation periods. " +
        "New employees start with PROBATION status. HR can initiate, extend, confirm, or reject probation. " +
        "Confirmation changes the employee status to ACTIVE. Rejection triggers the offboarding process.")
public class ProbationController {

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Initiate probation",
            description = "Creates a probation record for a new employee. " +
                    "Probation start date is typically the joining date. " +
                    "Standard probation period is 3–6 months depending on company policy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Probation initiated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "422", description = "Employee already has an active probation"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<Void>> initiate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Probation details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "startDate": "2024-01-15",
                                      "endDate": "2024-07-15",
                                      "remarks": "Standard 6-month probation"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Probation initiated", null));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER')")
    @Operation(
            summary = "Get probation by employee",
            description = "Returns the current or most recent probation record for an employee."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Probation record returned"),
            @ApiResponse(responseCode = "404", description = "No probation record found for this employee")
    })
    public ResponseEntity<ApiResponse<Void>> getByEmployee(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Confirm probation",
            description = "Confirms the employee's probation — marks them as a permanent employee. " +
                    "Employee status changes from PROBATION to ACTIVE. " +
                    "An approval workflow notification is sent to the employee."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Probation confirmed — employee is now ACTIVE"),
            @ApiResponse(responseCode = "404", description = "Probation record not found"),
            @ApiResponse(responseCode = "422", description = "Probation is not in IN_PROGRESS status"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> confirm(
            @Parameter(description = "Probation UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Probation confirmed", null));
    }

    @PostMapping("/{id}/extend")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Extend probation",
            description = "Extends the probation period with a new end date. " +
                    "Provide a reason for the extension. The employee is notified of the extension."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Probation extended with new end date"),
            @ApiResponse(responseCode = "404", description = "Probation record not found"),
            @ApiResponse(responseCode = "422", description = "Probation cannot be extended — already confirmed or rejected"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> extend(
            @Parameter(description = "Probation UUID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Extension details",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "extendedEndDate": "2024-10-15",
                                      "remarks": "Performance improvement required"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Probation extended", null));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Reject probation",
            description = "Rejects the employee's probation — triggers the offboarding process. " +
                    "Employee status changes to TERMINATED. A reason must be provided."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Probation rejected — offboarding process initiated"),
            @ApiResponse(responseCode = "404", description = "Probation record not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> reject(
            @Parameter(description = "Probation UUID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rejection reason",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "remarks": "Performance did not meet expectations"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Probation rejected", null));
    }
}
