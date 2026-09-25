package com.hrms.onboarding.controller;

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
@RequestMapping("/onboarding")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Onboarding Management", description = "Manage employee onboarding checklists and tasks. " +
        "Onboarding is initiated when a new employee is created. " +
        "Default checklist tasks: Create User Account, Assign Department, Assign Manager, Assign Assets, Upload Documents, Send Welcome Email. " +
        "Each task can be assigned to a specific HR/IT person with a due date.")
public class OnboardingController {

    @PostMapping("/employee/{employeeId}/initiate")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Initiate onboarding",
            description = "Creates the onboarding checklist for a new employee. " +
                    "Default tasks are auto-created based on company onboarding template. " +
                    "Each task can be assigned to a responsible person with a due date."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Onboarding checklist created"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "422", description = "Onboarding already initiated for this employee"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> initiate(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Onboarding initiated", null));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(
            summary = "Get onboarding checklist",
            description = "Returns the onboarding checklist for an employee with task statuses. " +
                    "Task statuses: PENDING, IN_PROGRESS, COMPLETED, SKIPPED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboarding checklist returned"),
            @ApiResponse(responseCode = "404", description = "No onboarding record found for this employee")
    })
    public ResponseEntity<ApiResponse<Void>> getChecklist(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Mark onboarding task as complete",
            description = "Marks a specific onboarding task as completed. " +
                    "When all tasks are completed, the onboarding is marked as done and the employee is notified."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task marked as completed"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @Parameter(description = "Onboarding task UUID", required = true) @PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task completed", null));
    }

    @PostMapping("/tasks/{taskId}/add")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Add custom onboarding task",
            description = "Adds a custom task to an employee's onboarding checklist. " +
                    "Use this for company-specific onboarding steps not in the default template."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Custom task added to checklist"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> addTask(
            @Parameter(description = "Onboarding task UUID", required = true) @PathVariable UUID taskId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Custom task details",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "taskName": "Setup VPN Access",
                                      "taskDescription": "Configure VPN credentials for remote access",
                                      "assignedTo": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "dueDate": "2024-01-20"
                                    }
                                    """)
                    )
            )
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("Task added", null));
    }
}
