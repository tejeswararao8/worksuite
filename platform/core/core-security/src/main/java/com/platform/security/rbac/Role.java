package com.platform.security.rbac;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"name", "company_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false)
    private String name; // e.g. HR_MANAGER

    @Column
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false; // true = platform default, false = tenant custom

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
