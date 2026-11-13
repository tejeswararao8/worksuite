package com.platform.tenancy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenantLifecycleEventRepository extends JpaRepository<TenantLifecycleEvent, UUID> {

    List<TenantLifecycleEvent> findByTenantIdOrderByOccurredAtDesc(UUID tenantId);
}
