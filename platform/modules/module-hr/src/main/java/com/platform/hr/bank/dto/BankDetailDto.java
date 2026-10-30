package com.platform.hr.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

public class BankDetailDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String bankName;
        @NotBlank private String accountHolderName;
        @NotBlank private String accountNumber;
        private String iban;
        private String swiftCode;
        private String branchName;
        private String branchAddress;
        private String currency = "AED";
        private Boolean isPrimary = true;
    }

    @Data
    public static class UpdateRequest {
        private String bankName;
        private String accountHolderName;
        private String accountNumber;
        private String iban;
        private String swiftCode;
        private String branchName;
        private String branchAddress;
        private String currency;
        private Boolean isPrimary;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private String bankName;
        private String accountHolderName;
        private String accountNumber;
        private String iban;
        private String swiftCode;
        private String branchName;
        private String currency;
        private Boolean isPrimary;
    }
}
