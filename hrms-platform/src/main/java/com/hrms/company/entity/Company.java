package com.hrms.company.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "website")
    private String website;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "industry")
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompanyStatus status;

    public enum CompanyStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
