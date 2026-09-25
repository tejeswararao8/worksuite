package com.hrms.auth.controller;

import com.hrms.auth.dto.AuthDto;
import com.hrms.auth.service.AuthService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for login, token refresh, and password management. No JWT token required for login and refresh.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with email and password. Returns a JWT access token and refresh token. " +
                    "The access token expires in 24 hours. Account is locked after 5 consecutive failed attempts for 30 minutes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — returns access and refresh tokens"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "423", description = "Account locked due to too many failed attempts"),
            @ApiResponse(responseCode = "400", description = "Validation error — email or password missing")
    })
    public ResponseEntity<com.hrms.common.dto.ApiResponse<AuthDto.TokenResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "admin@company.com",
                                      "password": "Admin@1234"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(com.hrms.common.dto.ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for a new access token. " +
                    "Use this when the access token has expired. Refresh tokens are valid for 7 days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token issued"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired"),
            @ApiResponse(responseCode = "400", description = "Refresh token is missing")
    })
    public ResponseEntity<com.hrms.common.dto.ApiResponse<AuthDto.TokenResponse>> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token obtained from login",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AuthDto.RefreshRequest request) {
        return ResponseEntity.ok(com.hrms.common.dto.ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change password",
            description = "Change the currently authenticated user's password. " +
                    "Requires the current password for verification. " +
                    "After a successful change, the user must log in again with the new password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password is incorrect or new password fails validation"),
            @ApiResponse(responseCode = "401", description = "Not authenticated — JWT token missing or expired")
    })
    public ResponseEntity<com.hrms.common.dto.ApiResponse<Void>> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current and new password",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "currentPassword": "Admin@1234",
                                      "newPassword": "NewPass@5678"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getCurrentUsername(), request);
        return ResponseEntity.ok(com.hrms.common.dto.ApiResponse.success("Password changed successfully", null));
    }
}
