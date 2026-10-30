package com.platform.hr.probation.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.probation.dto.ProbationDto;
import com.platform.hr.probation.entity.Probation.ProbationStatus;
import com.platform.hr.probation.service.ProbationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/probations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Probation")
public class ProbationController {

    private final ProbationService probationService;

    // ── ADMIN: List all probations ─────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROBATION_READ')")
    @Operation(summary = "List all probations (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<ProbationDto.Response>>> findAll(
            @RequestParam(required = false) ProbationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            probationService.findAll(status, PageRequest.of(page, size, Sort.by("endDate").ascending()))
        ));
    }

    // ── EMPLOYEE/MANAGER: Get own or direct report probation ───
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('PROBATION_READ')")
    @Operation(summary = "Get active probation for employee")
    public ResponseEntity<ApiResponse<ProbationDto.Response>> findByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(probationService.findByEmployee(employeeId)));
    }

    // ── ADMIN/HR: Confirm probation ────────────────────────────
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('PROBATION_WRITE')")
    @Operation(summary = "Confirm probation — employee becomes ACTIVE")
    public ResponseEntity<ApiResponse<ProbationDto.Response>> confirm(
            @PathVariable UUID id,
            @Valid @RequestBody ProbationDto.ConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Probation confirmed", probationService.confirm(id, request)));
    }

    // ── ADMIN/HR: Extend probation ─────────────────────────────
    @PutMapping("/{id}/extend")
    @PreAuthorize("hasAnyAuthority('PROBATION_WRITE')")
    @Operation(summary = "Extend probation period")
    public ResponseEntity<ApiResponse<ProbationDto.Response>> extend(
            @PathVariable UUID id,
            @Valid @RequestBody ProbationDto.ExtendRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Probation extended", probationService.extend(id, request)));
    }

    // ── ADMIN/HR: Reject probation ─────────────────────────────
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('PROBATION_WRITE')")
    @Operation(summary = "Reject probation — employee gets TERMINATED")
    public ResponseEntity<ApiResponse<ProbationDto.Response>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody ProbationDto.RejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Probation rejected", probationService.reject(id, request)));
    }
}
