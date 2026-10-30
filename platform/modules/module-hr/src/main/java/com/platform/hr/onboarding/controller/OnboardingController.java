package com.platform.hr.onboarding.controller;

import com.platform.api.ApiResponse;
import com.platform.hr.onboarding.dto.OnboardingDto;
import com.platform.hr.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/onboarding")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/employees/{employeeId}/tasks")
    @PreAuthorize("hasAnyAuthority('ONBOARDING_WRITE')")
    @Operation(summary = "Add onboarding task for employee")
    public ResponseEntity<ApiResponse<OnboardingDto.Response>> addTask(
            @PathVariable UUID employeeId,
            @Valid @RequestBody OnboardingDto.CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task added", onboardingService.addTask(employeeId, request)));
    }

    @GetMapping("/employees/{employeeId}/tasks")
    @PreAuthorize("hasAnyAuthority('ONBOARDING_READ')")
    @Operation(summary = "Get all onboarding tasks for employee")
    public ResponseEntity<ApiResponse<List<OnboardingDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.findByEmployee(employeeId)));
    }

    @GetMapping("/employees/{employeeId}/progress")
    @PreAuthorize("hasAnyAuthority('ONBOARDING_READ')")
    @Operation(summary = "Get onboarding progress for employee")
    public ResponseEntity<ApiResponse<OnboardingDto.ProgressResponse>> getProgress(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.getProgress(employeeId)));
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tasks assigned to me")
    public ResponseEntity<ApiResponse<List<OnboardingDto.Response>>> myTasks() {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.findMyAssignedTasks()));
    }

    @PutMapping("/tasks/{taskId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark task as complete")
    public ResponseEntity<ApiResponse<OnboardingDto.Response>> completeTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task completed", onboardingService.completeTask(taskId)));
    }

    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyAuthority('ONBOARDING_WRITE')")
    @Operation(summary = "Update onboarding task")
    public ResponseEntity<ApiResponse<OnboardingDto.Response>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody OnboardingDto.UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", onboardingService.updateTask(taskId, request)));
    }
}
