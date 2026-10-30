package com.platform.hr.team.controller;

import com.platform.api.ApiResponse;
import com.platform.api.PagedResponse;
import com.platform.hr.team.dto.TeamDto;
import com.platform.hr.team.service.TeamService;
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
@RequestMapping("/api/v1/hr/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Teams")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TEAM_WRITE')")
    @Operation(summary = "Create team")
    public ResponseEntity<ApiResponse<TeamDto.Response>> create(
            @Valid @RequestBody TeamDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Team created", teamService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEAM_READ')")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TEAM_READ')")
    @Operation(summary = "Search teams")
    public ResponseEntity<ApiResponse<PagedResponse<TeamDto.Response>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            teamService.search(q, departmentId, PageRequest.of(page, size, Sort.by("name").ascending()))
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active teams (for dropdowns)")
    public ResponseEntity<ApiResponse<List<TeamDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(teamService.findAll()));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyAuthority('TEAM_READ')")
    @Operation(summary = "Get team members")
    public ResponseEntity<ApiResponse<List<TeamDto.MemberResponse>>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getMembers(id)));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyAuthority('TEAM_WRITE')")
    @Operation(summary = "Add member to team")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody TeamDto.MemberRequest request) {
        teamService.addMember(id, request.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Member added", null));
    }

    @DeleteMapping("/{id}/members/{employeeId}")
    @PreAuthorize("hasAnyAuthority('TEAM_WRITE')")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID employeeId) {
        teamService.removeMember(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEAM_WRITE')")
    @Operation(summary = "Update team")
    public ResponseEntity<ApiResponse<TeamDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TeamDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEAM_WRITE')")
    @Operation(summary = "Deactivate team")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        teamService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Team deactivated", null));
    }
}
