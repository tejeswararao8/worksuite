package com.hrms.team.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.team.dto.TeamDto;
import com.hrms.team.service.TeamService;
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

import java.util.UUID;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Team Management", description = "Manage teams within departments. " +
        "Teams are sub-groups inside a department (e.g. Backend Team inside Engineering). " +
        "Each team can have a team lead assigned.")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Create team",
            description = "Creates a new team under a department. " +
                    "The `departmentId` is required — a team must belong to a department. " +
                    "Optionally assign a team lead via `leadEmployeeId`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — name or departmentId missing"),
            @ApiResponse(responseCode = "403", description = "Access denied — COMPANY_ADMIN or HR role required")
    })
    public ResponseEntity<ApiResponse<TeamDto.Response>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Team details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Backend Team",
                                      "description": "Responsible for server-side development",
                                      "departmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "leadEmployeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa7"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TeamDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created", teamService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get team by ID",
            description = "Returns details of a specific team including its department and team lead references."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team details returned"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<TeamDto.Response>> findById(
            @Parameter(description = "Team UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all teams",
            description = "Returns a paginated list of all active teams in the company, sorted alphabetically by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of teams"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<TeamDto.Response>>> findAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.findAll(PageRequest.of(page, size, Sort.by("name")))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Update team",
            description = "Updates team details. You can reassign the team lead or move the team to a different department."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team updated successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<TeamDto.Response>> update(
            @Parameter(description = "Team UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody TeamDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'HR')")
    @Operation(
            summary = "Delete team",
            description = "Soft-deletes a team. Employees assigned to this team should be reassigned first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Team UUID", required = true) @PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted", null));
    }
}
