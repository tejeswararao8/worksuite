package com.platform.hr.asset.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.asset.dto.AssetDto;
import com.platform.hr.asset.entity.Asset.AssetStatus;
import com.platform.hr.asset.entity.Asset.AssetType;
import com.platform.hr.asset.service.AssetService;
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
@RequestMapping("/api/v1/hr/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Assets")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ASSET_WRITE')")
    @Operation(summary = "Add new asset")
    public ResponseEntity<ApiResponse<AssetDto.Response>> create(
            @Valid @RequestBody AssetDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Asset created", assetService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ASSET_READ')")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<ApiResponse<AssetDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ASSET_READ')")
    @Operation(summary = "Search assets")
    public ResponseEntity<ApiResponse<PagedResponse<AssetDto.Response>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            assetService.search(q, status, assetType,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ASSET_READ')")
    @Operation(summary = "Get assets assigned to employee")
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> findByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findByEmployee(employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ASSET_WRITE')")
    @Operation(summary = "Update asset details")
    public ResponseEntity<ApiResponse<AssetDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Asset updated", assetService.update(id, request)));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ASSET_WRITE')")
    @Operation(summary = "Assign asset to employee")
    public ResponseEntity<ApiResponse<AssetDto.Response>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssetDto.AssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Asset assigned", assetService.assign(id, request)));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyAuthority('ASSET_WRITE')")
    @Operation(summary = "Return asset from employee")
    public ResponseEntity<ApiResponse<AssetDto.Response>> returnAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AssetDto.ReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Asset returned", assetService.returnAsset(id, request)));
    }

    @DeleteMapping("/{id}/retire")
    @PreAuthorize("hasAnyAuthority('ASSET_WRITE')")
    @Operation(summary = "Retire asset")
    public ResponseEntity<ApiResponse<Void>> retire(@PathVariable UUID id) {
        assetService.retire(id);
        return ResponseEntity.ok(ApiResponse.success("Asset retired", null));
    }
}
