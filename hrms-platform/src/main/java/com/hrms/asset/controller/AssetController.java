package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetDto;
import com.hrms.asset.service.AssetService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Asset Management", description = "Manage company assets (Laptop, Desktop, Mobile, Monitor, SIM, Access Card, etc.). " +
        "Track asset assignment, return, warranty, and repair history. " +
        "Asset codes are auto-generated (e.g. AST00001). " +
        "Assets can only be assigned to one employee at a time.")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create asset",
            description = "Registers a new asset in the company inventory. " +
                    "An auto-generated asset code (e.g. AST00001) is assigned. " +
                    "Asset types: LAPTOP, DESKTOP, MOBILE, MONITOR, SIM, ACCESS_CARD, OTHER. " +
                    "New assets start with AVAILABLE status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asset created with auto-generated asset code"),
            @ApiResponse(responseCode = "400", description = "Validation error — name or assetType missing"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<AssetDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Asset details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "MacBook Pro 14",
                                      "assetType": "LAPTOP",
                                      "serialNumber": "C02XG2JHJGH5",
                                      "model": "MacBook Pro 14-inch M3",
                                      "brand": "Apple",
                                      "purchaseDate": "2024-01-10",
                                      "warrantyExpiry": "2027-01-10",
                                      "notes": "Purchased for engineering team"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AssetDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created", assetService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get asset by ID",
            description = "Returns full details of an asset including its current status, assigned employee, and assignment history."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset details returned"),
            @ApiResponse(responseCode = "404", description = "Asset not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<AssetDto.Response>> findById(
            @Parameter(description = "Asset UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "List all assets",
            description = "Returns a paginated list of all assets in the company inventory, sorted by creation date descending. " +
                    "Includes available, assigned, in-repair, and retired assets."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of assets"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AssetDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                assetService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Get assets assigned to an employee",
            description = "Returns all assets currently assigned to a specific employee. " +
                    "Employees can view their own assigned assets. Managers can view their team's assets."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of assets assigned to the employee"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> findByEmployee(
            @Parameter(description = "Employee UUID", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findByEmployee(employeeId)));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Assign asset to employee",
            description = "Assigns an available asset to an employee. " +
                    "The asset status changes from AVAILABLE to ASSIGNED. " +
                    "Assignment timestamp is recorded. " +
                    "An asset can only be assigned to one employee at a time — return it first before reassigning."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Asset not found"),
            @ApiResponse(responseCode = "422", description = "Asset is not available — it is already assigned or in repair"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<AssetDto.Response>> assign(
            @Parameter(description = "Asset UUID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee to assign the asset to",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AssetDto.AssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Asset assigned", assetService.assign(id, request)));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Return asset",
            description = "Marks an assigned asset as returned. " +
                    "The asset status changes back to AVAILABLE. " +
                    "Return timestamp is recorded. The asset can then be reassigned to another employee."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset returned successfully — status is now AVAILABLE"),
            @ApiResponse(responseCode = "404", description = "Asset not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<AssetDto.Response>> returnAsset(
            @Parameter(description = "Asset UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Asset returned", assetService.returnAsset(id)));
    }
}
