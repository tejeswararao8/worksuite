package com.platform.hr.department.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.department.dto.DepartmentDto;
import com.platform.hr.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_WRITE')")
    @Operation(summary = "Create department")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> create(
            @Valid @RequestBody DepartmentDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Department created", departmentService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Search departments")
    public ResponseEntity<ApiResponse<PagedResponse<DepartmentDto.Response>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            departmentService.search(q, PageRequest.of(page, size, Sort.by("name").ascending()))
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active departments (for dropdowns)")
    public ResponseEntity<ApiResponse<List<DepartmentDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.findAll()));
    }

    @GetMapping("/org-chart")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department org chart tree")
    public ResponseEntity<ApiResponse<List<DepartmentDto.OrgNode>>> getOrgChart() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getOrgChart()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_WRITE')")
    @Operation(summary = "Update department")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", departmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_WRITE')")
    @Operation(summary = "Deactivate department")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        departmentService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated", null));
    }
}
