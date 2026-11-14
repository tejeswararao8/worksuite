package com.platform.hr.invitation;

import com.platform.api.ApiResponse;
import com.platform.hr.subscription.EnforceModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/hr/invitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HR - Employee Invitations")
public class TenantInvitationController {

    private final TenantInvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @EnforceModule("HR")
    @Operation(summary = "Invite an employee")
    public ResponseEntity<ApiResponse<TenantInvitationDto.Response>> invite(
            @Valid @RequestBody TenantInvitationDto.InviteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent", invitationService.invite(req)));
    }

    /**
     * Public acceptance endpoint — authenticated but no tenant role required.
     * The token is in the POST body, never in a query string.
     */
    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept an employee invitation")
    public ResponseEntity<ApiResponse<TenantInvitationDto.Response>> accept(
            @Valid @RequestBody TenantInvitationDto.AcceptRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation accepted", invitationService.acceptInvitation(req.getToken())));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ')")
    @Operation(summary = "List invitations for the current tenant")
    public ResponseEntity<ApiResponse<Page<TenantInvitationDto.Response>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TenantEmployeeInvitation.InvitationStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                invitationService.listInvitations(page, size, status)));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @EnforceModule("HR")
    @Operation(summary = "Resend an invitation (rotates token)")
    public ResponseEntity<ApiResponse<TenantInvitationDto.Response>> resend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation resent", invitationService.resendInvitation(id)));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_WRITE')")
    @EnforceModule("HR")
    @Operation(summary = "Revoke a pending invitation")
    public ResponseEntity<ApiResponse<TenantInvitationDto.Response>> revoke(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation revoked", invitationService.revokeInvitation(id)));
    }
}
