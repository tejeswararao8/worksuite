package com.hrms.branch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class BranchDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        @NotBlank
        private String code;
        private String address;
        private String city;
        private String country;
        private String phone;
        private String email;
        private boolean headOffice;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String code;
        private String address;
        private String city;
        private String country;
        private String phone;
        private String email;
        private boolean headOffice;
        private LocalDateTime createdAt;
    }
}
