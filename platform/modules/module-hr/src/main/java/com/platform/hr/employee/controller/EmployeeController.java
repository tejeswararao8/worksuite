package com.platform.hr.employee.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.employee.dto.EmployeeDto;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import com.platform.hr.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/hr/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Create employee")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> create(@Valid @RequestBody EmployeeDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Employee created", employeeService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Search employees")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeDto.Response>>> search(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) UUID departmentId,
        @RequestParam(required = false) UUID branchId,
        @RequestParam(required = false) EmploymentStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            employeeService.search(q, departmentId, branchId, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Update employee")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> update(
        @PathVariable UUID id, @Valid @RequestBody EmployeeDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.update(id, request)));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Upload employee photo")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> uploadPhoto(
        @PathVariable UUID id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded", employeeService.uploadPhoto(id, file)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Delete employee")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
    }
}
