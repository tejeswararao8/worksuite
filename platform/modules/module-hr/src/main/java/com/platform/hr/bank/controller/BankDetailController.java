package com.platform.hr.bank.controller;

import com.platform.api.ApiResponse;
import com.platform.hr.bank.dto.BankDetailDto;
import com.platform.hr.bank.service.BankDetailService;
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
@RequestMapping("/api/v1/hr/employees/{employeeId}/bank-details")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Bank Details")
public class BankDetailController {

    private final BankDetailService bankDetailService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Add bank detail")
    public ResponseEntity<ApiResponse<BankDetailDto.Response>> create(
            @PathVariable UUID employeeId,
            @Valid @RequestBody BankDetailDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Bank detail added", bankDetailService.create(employeeId, request)));
    }

    // HR_ADMIN only — bank details are sensitive
    @GetMapping
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'FINANCE_ADMIN')")
    @Operation(summary = "Get bank details (HR Admin / Finance Admin only)")
    public ResponseEntity<ApiResponse<List<BankDetailDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(bankDetailService.findByEmployee(employeeId)));
    }

    @PutMapping("/{bankId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Update bank detail")
    public ResponseEntity<ApiResponse<BankDetailDto.Response>> update(
            @PathVariable UUID employeeId,
            @PathVariable UUID bankId,
            @Valid @RequestBody BankDetailDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bank detail updated",
            bankDetailService.update(employeeId, bankId, request)));
    }

    @DeleteMapping("/{bankId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @Operation(summary = "Delete bank detail")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID employeeId,
            @PathVariable UUID bankId) {
        bankDetailService.delete(employeeId, bankId);
        return ResponseEntity.ok(ApiResponse.success("Bank detail deleted", null));
    }
}
