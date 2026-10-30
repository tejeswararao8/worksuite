package com.platform.hr.promotion.dto;

import com.platform.hr.promotion.entity.Promotion.PromotionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromotionDto {

    @Data
    public static class CreateRequest {
        @NotNull private UUID employeeId;
        @NotNull private UUID toDesignationId;
        @NotNull private LocalDate effectiveDate;
        private String reason;
    }

    @Data
    public static class ApproveRequest {
        private String comments;
    }

    @Data
    public static class RejectRequest {
        @NotNull private String reason;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private String employeeName;
        private String employeeCode;
        private UUID fromDesignationId;
        private String fromDesignationTitle;
        private UUID toDesignationId;
        private String toDesignationTitle;
        private LocalDate effectiveDate;
        private String reason;
        private PromotionStatus status;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
    }
}
