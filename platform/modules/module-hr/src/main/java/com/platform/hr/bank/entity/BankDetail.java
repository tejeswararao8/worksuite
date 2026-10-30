package com.platform.hr.bank.entity;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "bank_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankDetail extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "iban")
    private String iban;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "branch_address")
    private String branchAddress;

    @Column(name = "currency", nullable = false)
    private String currency = "AED";

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;
}
