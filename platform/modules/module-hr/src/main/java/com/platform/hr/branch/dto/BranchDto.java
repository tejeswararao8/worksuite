package com.platform.hr.branch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class BranchDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String address;
        private String city;
        private String country;
        private String phone;
        @Email private String email;
        private boolean headOffice = false;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String address;
        private String city;
        private String country;
        private String phone;
        @Email private String email;
        private Boolean headOffice;
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
        private long employeeCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private UUID id;
        private String name;
        private String code;
        private String city;
        private String country;
    }
}
