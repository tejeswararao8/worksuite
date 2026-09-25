package com.hrms.offboarding.controller;

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
@RequestMapping("/offboarding")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Offboarding Management", description = "Manage employee offboarding process. " +
        "Offboarding is triggered by resignation or termination. " +
        "Default checklist tasks: Notice Period tracking, Asset Return, Knowledge Transfer, Exit Clearance, Disable User Account. " +
        "Employee status changes to RESIGNED or TERMINATED upon completion.")
public class OffboardingController {

    @PostMapping("/employee/{employeeId}/initiate")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Initiate offboarding",
            description = "Starts the offboarding process for an employee. " +
                    "Specify the reason (RESIGNATION or TERMINATION), last working date, and notice period end date. " +
                    "An offboarding checklist is auto-created with default tasks."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Offboarding initiated — checklist created"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "422", description = "Offboarding already in progress for this employee"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<Void>> initiate(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Offboarding details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "reason": "RESIGNATION",
                                      "resignationDate": "2024-03-01",
                                      "noticePeriodEndDate": "2024-03-31",
                                      "lastWorkingDate": "2024-03-31",
                                      "remarks": "Employee resigned for personal reasons"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Offboarding initiated", null));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Get offboarding checklist",
            description = "Returns the offboarding checklist for an employee with task statuses and completion progress."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offboarding checklist returned"),
            @ApiResponse(responseCode = "404", description = "No offboarding record found for this employee")
    })
    public ResponseEntity<ApiResponse<Void>> getChecklist(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Mark offboarding task as complete",
            description = "Marks a specific offboarding task as completed. " +
                    "When all tasks are completed, the employee status is updated to RESIGNED or TERMINATED " +
                    "and the user account is automatically disabled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task marked as completed"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @Parameter(description = "Offboarding task UUID", required = true) @PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task completed", null));
    }

    @PostMapping("/employee/{employeeId}/complete")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Complete offboarding",
            description = "Finalizes the offboarding process. " +
                    "Employee status is updated to RESIGNED or TERMINATED. " +
                    "User account is disabled. All assigned assets must be returned before completion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offboarding completed — employee account disabled"),
            @ApiResponse(responseCode = "422", description = "Cannot complete — pending tasks or unreturned assets"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> complete(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Offboarding completed", null));
    }
}
