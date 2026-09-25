package com.hrms.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeamDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private UUID departmentId;
        private UUID leadEmployeeId;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private UUID departmentId;
        private UUID leadEmployeeId;
        private LocalDateTime createdAt;
    }
}
