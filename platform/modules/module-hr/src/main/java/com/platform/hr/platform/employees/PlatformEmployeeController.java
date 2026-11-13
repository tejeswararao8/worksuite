package com.platform.hr.platform.employees;

import com.platform.api.ApiResponse;
import com.platform.security.rbac.PlatformRoles;
import com.platform.tenancy.membership.MembershipStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Platform employee management REST API.
 *
 * <p>All endpoints require a platform role (enforced by SecurityConfig URL guard).
 * Destructive actions additionally require PLATFORM_SUPER_ADMIN or PLATFORM_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/platform/employees")
@RequiredArgsConstructor
public class PlatformEmployeeController {

    private final PlatformEmployeeService employeeService;

    private static final String ADMIN_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "')";

    private static final String ALL_PLATFORM_ROLES =
            "hasAnyAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "', '" +
            PlatformRoles.PLATFORM_ADMIN + "', '" +
            PlatformRoles.PLATFORM_SUPPORT + "', '" +
            PlatformRoles.PLATFORM_IMPLEMENTATION + "')";

    // ── List employees ────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<Page<PlatformEmployeeDto>>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    MembershipStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.listEmployees(page, size, status)));
    }

    // ── Get employee ──────────────────────────────────────────────────────────

    @GetMapping("/{membershipId}")
    @PreAuthorize(ALL_PLATFORM_ROLES)
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> getById(
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getEmployee(membershipId)));
    }

    // ── Invite ────────────────────────────────────────────────────────────────

    @PostMapping("/invite")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<InvitationDto>> invite(
            @Valid @RequestBody InviteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent", employeeService.invite(req)));
    }

    // ── Accept invitation ─────────────────────────────────────────────────────

    /**
     * Accepts a platform invitation.
     * The authenticated user must have an email matching the invitation.
     * The role is taken from the invitation record — no role parameter is accepted.
     * Any platform role is sufficient to call this endpoint (the user is not yet a member).
     */
    @PostMapping("/invitations/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> acceptInvitation(
            @RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation accepted", employeeService.acceptInvitation(token)));
    }

    // ── List invitations ──────────────────────────────────────────────────────

    @GetMapping("/invitations")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<Page<InvitationDto>>> listInvitations(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    PlatformInvitation.InvitationStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.listInvitations(page, size, status)));
    }

    // ── Resend invitation ─────────────────────────────────────────────────────

    @PostMapping("/invitations/{id}/resend")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<InvitationDto>> resend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation resent", employeeService.resendInvitation(id)));
    }

    // ── Revoke invitation ─────────────────────────────────────────────────────

    @PostMapping("/invitations/{id}/revoke")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<InvitationDto>> revokeInvitation(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation revoked", employeeService.revokeInvitation(id)));
    }

    // ── Change role ───────────────────────────────────────────────────────────

    @PutMapping("/{membershipId}/role")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> changeRole(
            @PathVariable UUID membershipId,
            @Valid @RequestBody RoleChangeRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Role updated", employeeService.changeRole(membershipId, req.getRoleName())));
    }

    // ── Suspend ───────────────────────────────────────────────────────────────

    @PostMapping("/{membershipId}/suspend")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> suspend(
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Employee suspended", employeeService.suspendEmployee(membershipId)));
    }

    // ── Reactivate ────────────────────────────────────────────────────────────

    @PostMapping("/{membershipId}/reactivate")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> reactivate(
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Employee reactivated", employeeService.reactivateEmployee(membershipId)));
    }

    // ── Revoke ────────────────────────────────────────────────────────────────

    @PostMapping("/{membershipId}/revoke")
    @PreAuthorize("hasAuthority('" + PlatformRoles.PLATFORM_SUPER_ADMIN + "')")
    public ResponseEntity<ApiResponse<PlatformEmployeeDto>> revoke(
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Employee access revoked", employeeService.revokeEmployee(membershipId)));
    }
}
