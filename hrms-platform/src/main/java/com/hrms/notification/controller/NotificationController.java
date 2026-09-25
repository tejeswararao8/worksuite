package com.hrms.notification.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PagedResponse;
import com.hrms.notification.dto.NotificationDto;
import com.hrms.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "In-app notification center for the authenticated user. " +
        "Notifications are generated automatically by the system for events such as: " +
        "document expiry reminders (90/60/30/7 days), onboarding tasks, probation confirmations, " +
        "transfer approvals, promotion approvals, and asset assignments.")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my notifications",
            description = "Returns a paginated list of all in-app notifications for the currently authenticated user, " +
                    "sorted by creation date descending (newest first). " +
                    "Includes both read and unread notifications. " +
                    "Notification types: DOCUMENT_EXPIRY, ONBOARDING, OFFBOARDING, PROBATION, TRANSFER, PROMOTION, ASSET, GENERAL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of notifications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDto.Response>>> getMyNotifications(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get unread notification count",
            description = "Returns the total count of unread notifications for the authenticated user. " +
                    "Use this to display a badge/counter on the notification bell icon in the UI."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notification count returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<NotificationDto.UnreadCount>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark a single notification as read",
            description = "Marks a specific notification as read by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "Notification UUID", required = true) @PathVariable java.util.UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all unread notifications for the authenticated user as read. " +
                    "After this call, the unread count will return 0. " +
                    "This is typically triggered when the user opens the notification panel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
