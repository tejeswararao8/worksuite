package com.platform.hr.offboarding.controller;

import com.platform.api.ApiResponse;
import com.platform.hr.offboarding.dto.OffboardingDto;
import com.platform.hr.offboarding.service.OffboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/offboarding")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Offboarding")
public class OffboardingController {

    private final OffboardingService offboardingService;

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyAuthority('OFFBOARDING_WRITE')")
    @Operation(summary = "Initiate offboarding for employee")
    public ResponseEntity<ApiResponse<OffboardingDto.ProgressResponse>> initiate(
            @Valid @RequestBody OffboardingDto.InitiateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Offboarding initiated", offboardingService.initiate(request)));
    }

    @GetMapping("/employees/{employeeId}/tasks")
    @PreAuthorize("hasAnyAuthority('OFFBOARDING_READ')")
    @Operation(summary = "Get offboarding tasks for employee")
    public ResponseEntity<ApiResponse<List<OffboardingDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(offboardingService.findByEmployee(employeeId)));
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get offboarding tasks assigned to me")
    public ResponseEntity<ApiResponse<List<OffboardingDto.Response>>> myTasks() {
        return ResponseEntity.ok(ApiResponse.success(offboardingService.findMyAssignedTasks()));
    }

    @PostMapping("/employees/{employeeId}/tasks")
    @PreAuthorize("hasAnyAuthority('OFFBOARDING_WRITE')")
    @Operation(summary = "Add offboarding task")
    public ResponseEntity<ApiResponse<OffboardingDto.Response>> addTask(
            @PathVariable UUID employeeId,
            @Valid @RequestBody OffboardingDto.CreateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task added", offboardingService.addTask(employeeId, request)));
    }

    @PutMapping("/tasks/{taskId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark offboarding task as complete")
    public ResponseEntity<ApiResponse<OffboardingDto.Response>> completeTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task completed", offboardingService.completeTask(taskId)));
    }
}
