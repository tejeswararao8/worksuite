package com.platform.security.rbac;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. EMPLOYEE_READ, EMPLOYEE_WRITE

    @Column
    private String description;

    @Column(nullable = false)
    private String module; // e.g. HR, FINANCE
}
