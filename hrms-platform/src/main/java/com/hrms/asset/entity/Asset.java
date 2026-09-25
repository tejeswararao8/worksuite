package com.hrms.asset.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {

    @Column(name = "asset_code", nullable = false)
    private String assetCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "model")
    private String model;

    @Column(name = "brand")
    private String brand;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssetStatus status;

    @Column(name = "assigned_to_employee_id")
    private UUID assignedToEmployeeId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum AssetType {
        LAPTOP, DESKTOP, MOBILE, MONITOR, SIM, ACCESS_CARD, OTHER
    }

    public enum AssetStatus {
        AVAILABLE, ASSIGNED, IN_REPAIR, RETIRED
    }
}
