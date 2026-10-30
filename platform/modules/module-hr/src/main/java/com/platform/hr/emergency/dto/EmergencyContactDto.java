package com.platform.hr.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

public class EmergencyContactDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String name;
        @NotBlank private String relationship;
        @NotBlank private String phone;
        private String alternatePhone;
        private String email;
        private String address;
        private Boolean isPrimary = false;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String relationship;
        private String phone;
        private String alternatePhone;
        private String email;
        private String address;
        private Boolean isPrimary;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private String name;
        private String relationship;
        private String phone;
        private String alternatePhone;
        private String email;
        private String address;
        private Boolean isPrimary;
    }
}
