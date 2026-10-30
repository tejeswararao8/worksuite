package com.platform.hr.branch.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.branch.dto.BranchDto;
import com.platform.hr.branch.service.BranchService;
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
@RequestMapping("/api/v1/hr/branches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Branches")
public class BranchController {

    private final BranchService branchService;

    // ── ADMIN: Create branch ───────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyAuthority('BRANCH_WRITE')")
    @Operation(summary = "Create a new branch")
    public ResponseEntity<ApiResponse<BranchDto.Response>> create(
            @Valid @RequestBody BranchDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Branch created", branchService.create(request)));
    }

    // ── ADMIN: Get branch by ID ────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BRANCH_READ')")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.findById(id)));
    }

    // ── ADMIN: Search branches ─────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('BRANCH_READ')")
    @Operation(summary = "Search branches")
    public ResponseEntity<ApiResponse<PagedResponse<BranchDto.Response>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            branchService.search(q, PageRequest.of(page, size, Sort.by("name").ascending()))
        ));
    }

    // ── ALL: Get all branches as dropdown list ─────────────────
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active branches (for dropdowns)")
    public ResponseEntity<ApiResponse<List<BranchDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(branchService.findAll()));
    }

    // ── ADMIN: Update branch ───────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BRANCH_WRITE')")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BranchDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.update(id, request)));
    }

    // ── ADMIN: Deactivate branch ───────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BRANCH_WRITE')")
    @Operation(summary = "Deactivate branch (only if no active employees)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        branchService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deactivated", null));
    }
}
