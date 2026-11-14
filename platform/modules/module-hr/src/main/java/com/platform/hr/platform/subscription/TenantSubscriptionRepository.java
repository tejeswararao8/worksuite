package com.platform.hr.platform.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, UUID> {

    /**
     * Returns the single non-terminal subscription for a tenant, if any.
     * Non-terminal = TRIAL, ACTIVE, PAST_DUE, SUSPENDED.
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId " +
           "AND s.status NOT IN ('EXPIRED', 'CANCELLED')")
    Optional<TenantSubscription> findActiveByTenantId(@Param("tenantId") UUID tenantId);

    /** All subscriptions for a tenant (including historical). */
    Page<TenantSubscription> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    /** All subscriptions across all tenants, newest first. */
    Page<TenantSubscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Count non-terminal subscriptions by status. */
    long countByStatus(TenantSubscription.SubscriptionStatus status);

    List<TenantSubscription> findByStatus(TenantSubscription.SubscriptionStatus status);
}
