package com.hrms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDto {

    @Data
    public static class CreateRequest {
        @Email @NotBlank private String email;
        @NotBlank        private String password;
        @NotNull         private UUID roleId;
        private UUID employeeId;
    }

    @Data
    public static class ChangeRoleRequest {
        @NotNull private UUID roleId;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank private String newPassword;
    }

    @Data
    public static class Response {
        private UUID id;
        private String email;
        private UUID employeeId;
        private String roleName;
        private boolean active;
        private LocalDateTime lastLoginAt;
        private boolean mustChangePassword;
    }
}
