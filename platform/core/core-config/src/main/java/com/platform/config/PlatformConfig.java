package com.platform.config;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platform_configs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"config_key", "company_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;
}
