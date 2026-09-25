package com.hrms.transfer.dto;

import com.hrms.transfer.entity.Transfer.TransferStatus;
import com.hrms.transfer.entity.Transfer.TransferType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
        @NotNull private TransferType transferType;
        @NotNull private LocalDate effectiveDate;
        private UUID toDepartmentId;
        private UUID toBranchId;
        private UUID toManagerId;
        private UUID toTeamId;
        private String reason;
    }

    @Data
    public static class ActionRequest {
        private String remarks;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private TransferType transferType;
        private UUID fromDepartmentId;
        private UUID toDepartmentId;
        private UUID fromBranchId;
        private UUID toBranchId;
        private UUID fromManagerId;
        private UUID toManagerId;
        private UUID fromTeamId;
        private UUID toTeamId;
        private LocalDate effectiveDate;
        private String reason;
        private TransferStatus status;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
    }
}
