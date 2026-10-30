package com.platform.hr.transfer.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.transfer.dto.TransferDto;
import com.platform.hr.transfer.entity.Transfer.TransferStatus;
import com.platform.hr.transfer.service.TransferService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TRANSFER_WRITE')")
    @Operation(summary = "Create transfer request")
    public ResponseEntity<ApiResponse<TransferDto.Response>> create(
            @Valid @RequestBody TransferDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Transfer request created", transferService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRANSFER_READ')")
    @Operation(summary = "List all transfers")
    public ResponseEntity<ApiResponse<PagedResponse<TransferDto.Response>>> findAll(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            transferService.findAll(status, PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSFER_READ')")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<ApiResponse<TransferDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transferService.findById(id)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('TRANSFER_READ')")
    @Operation(summary = "Get transfer history for employee")
    public ResponseEntity<ApiResponse<PagedResponse<TransferDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            transferService.findByEmployee(employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('TRANSFER_WRITE')")
    @Operation(summary = "Approve transfer request")
    public ResponseEntity<ApiResponse<TransferDto.Response>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody TransferDto.ApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer approved", transferService.approve(id, request)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('TRANSFER_WRITE')")
    @Operation(summary = "Reject transfer request")
    public ResponseEntity<ApiResponse<TransferDto.Response>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody TransferDto.RejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer rejected", transferService.reject(id, request)));
    }
}
