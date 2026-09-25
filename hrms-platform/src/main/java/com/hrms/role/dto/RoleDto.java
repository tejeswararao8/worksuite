package com.hrms.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

public class RoleDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String name;
        private String description;
        private Set<UUID> permissionIds;
    }

    @Data
    public static class UpdatePermissionsRequest {
        private Set<UUID> permissionIds;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private Set<PermissionResponse> permissions;
    }

    @Data
    public static class PermissionResponse {
        private UUID id;
        private String name;
        private String description;
        private String module;
    }
}
