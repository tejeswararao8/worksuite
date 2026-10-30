package com.platform.hr.emergency.entity;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "emergency_contacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relationship", nullable = false)
    private String relationship;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
}
