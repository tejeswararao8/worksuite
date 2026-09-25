package com.hrms.company.dto;

import com.hrms.company.entity.Company.CompanyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        private String legalName;
        private String registrationNumber;
        private String taxNumber;
        @Email
        private String email;
        private String phone;
        private String website;
        private String address;
        private String country;
        private String city;
        private String industry;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String legalName;
        private String registrationNumber;
        private String email;
        private String phone;
        private String website;
        private String address;
        private String country;
        private String city;
        private String logoUrl;
        private String industry;
        private CompanyStatus status;
        private LocalDateTime createdAt;
    }
}
