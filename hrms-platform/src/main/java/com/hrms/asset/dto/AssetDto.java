package com.hrms.asset.dto;

import com.hrms.asset.entity.Asset.AssetStatus;
import com.hrms.asset.entity.Asset.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AssetDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        @NotNull
        private AssetType assetType;
        private String serialNumber;
        private String model;
        private String brand;
        private LocalDate purchaseDate;
        private LocalDate warrantyExpiry;
        private String notes;
    }

    @Data
    public static class AssignRequest {
        @NotNull
        private UUID employeeId;
    }

    @Data
    public static class Response {
        private UUID id;
        private String assetCode;
        private String name;
        private AssetType assetType;
        private String serialNumber;
        private String model;
        private String brand;
        private LocalDate purchaseDate;
        private LocalDate warrantyExpiry;
        private AssetStatus status;
        private UUID assignedToEmployeeId;
        private LocalDateTime assignedAt;
        private LocalDateTime returnedAt;
        private LocalDateTime createdAt;
    }
}
