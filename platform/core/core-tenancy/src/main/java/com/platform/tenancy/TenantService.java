package com.platform.tenancy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public Tenant createTenant(String name, String slug, String email, String phone) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new TenantException("Slug already taken: " + slug);
        }
        if (tenantRepository.existsByEmail(email)) {
            throw new TenantException("Email already registered: " + email);
        }
        return tenantRepository.save(
            Tenant.builder()
                .name(name)
                .slug(slug)
                .email(email)
                .phone(phone)
                .status(Tenant.TenantStatus.ACTIVE)
                .isActive(true)
                .build()
        );
    }

    public Tenant getById(UUID id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new TenantException("Tenant not found: " + id));
    }

    public Tenant getBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
            .orElseThrow(() -> new TenantException("Tenant not found: " + slug));
    }

    @Transactional
    public void suspendTenant(UUID id) {
        Tenant tenant = getById(id);
        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);
    }
}
