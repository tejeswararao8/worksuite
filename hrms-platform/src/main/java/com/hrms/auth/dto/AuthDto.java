package com.hrms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UUID userId;
        private UUID companyId;
        private String email;
        private String role;

        public TokenResponse(String accessToken, String refreshToken, long expiresIn,
                             UUID userId, UUID companyId, String email, String role) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.userId = userId;
            this.companyId = companyId;
            this.email = email;
            this.role = role;
        }
    }

    @Data
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
    }
}
