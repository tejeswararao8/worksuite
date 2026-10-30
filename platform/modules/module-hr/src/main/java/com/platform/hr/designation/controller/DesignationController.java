package com.platform.hr.designation.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.designation.dto.DesignationDto;
import com.platform.hr.designation.service.DesignationService;
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
@RequestMapping("/api/v1/hr/designations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Designations")
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('DESIGNATION_WRITE')")
    @Operation(summary = "Create designation")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> create(
            @Valid @RequestBody DesignationDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Designation created", designationService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DESIGNATION_READ')")
    @Operation(summary = "Get designation by ID")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(designationService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DESIGNATION_READ')")
    @Operation(summary = "Search designations")
    public ResponseEntity<ApiResponse<PagedResponse<DesignationDto.Response>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            designationService.search(q, PageRequest.of(page, size, Sort.by("level").ascending()))
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active designations (for dropdowns)")
    public ResponseEntity<ApiResponse<List<DesignationDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(designationService.findAll()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DESIGNATION_WRITE')")
    @Operation(summary = "Update designation")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DesignationDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Designation updated", designationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DESIGNATION_WRITE')")
    @Operation(summary = "Deactivate designation")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        designationService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deactivated", null));
    }
}
