package com.platform.hr.promotion.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.promotion.dto.PromotionDto;
import com.platform.hr.promotion.entity.Promotion.PromotionStatus;
import com.platform.hr.promotion.service.PromotionService;
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
@RequestMapping("/api/v1/hr/promotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROMOTION_WRITE')")
    @Operation(summary = "Create promotion request")
    public ResponseEntity<ApiResponse<PromotionDto.Response>> create(
            @Valid @RequestBody PromotionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Promotion request created", promotionService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROMOTION_READ')")
    @Operation(summary = "List all promotions")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionDto.Response>>> findAll(
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            promotionService.findAll(status, PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('PROMOTION_READ')")
    @Operation(summary = "Get promotion history for employee")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionDto.Response>>> findByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            promotionService.findByEmployee(employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('PROMOTION_WRITE')")
    @Operation(summary = "Approve promotion")
    public ResponseEntity<ApiResponse<PromotionDto.Response>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionDto.ApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promotion approved", promotionService.approve(id, request)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('PROMOTION_WRITE')")
    @Operation(summary = "Reject promotion")
    public ResponseEntity<ApiResponse<PromotionDto.Response>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionDto.RejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promotion rejected", promotionService.reject(id, request)));
    }
}
