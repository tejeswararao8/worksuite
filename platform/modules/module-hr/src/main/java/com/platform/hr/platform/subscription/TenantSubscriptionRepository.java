package com.platform.hr.platform.subscription;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Pessimistic write lock — used when checking + updating storage quota
     * or employee limit to prevent concurrent over-allocation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId " +
           "AND s.status NOT IN ('EXPIRED', 'CANCELLED')")
    Optional<TenantSubscription> findActiveByTenantIdForUpdate(@Param("tenantId") UUID tenantId);

    /**
     * Atomically increments storage_used_bytes for the active subscription.
     * Returns the number of rows updated (0 if no active subscription found).
     */
    @Modifying
    @Query("UPDATE TenantSubscription s SET s.storageUsedBytes = s.storageUsedBytes + :delta " +
           "WHERE s.tenant.id = :tenantId AND s.status NOT IN ('EXPIRED', 'CANCELLED')")
    int incrementStorageUsed(@Param("tenantId") UUID tenantId, @Param("delta") long delta);

    /**
     * Atomically decrements storage_used_bytes, flooring at 0.
     */
    @Modifying
    @Query("UPDATE TenantSubscription s SET s.storageUsedBytes = " +
           "CASE WHEN s.storageUsedBytes >= :delta THEN s.storageUsedBytes - :delta ELSE 0 END " +
           "WHERE s.tenant.id = :tenantId AND s.status NOT IN ('EXPIRED', 'CANCELLED')")
    int decrementStorageUsed(@Param("tenantId") UUID tenantId, @Param("delta") long delta);

    /** All subscriptions for a tenant (including historical). */
    Page<TenantSubscription> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    /** All subscriptions across all tenants, newest first. */
    Page<TenantSubscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Count non-terminal subscriptions by status. */
    long countByStatus(TenantSubscription.SubscriptionStatus status);

    List<TenantSubscription> findByStatus(TenantSubscription.SubscriptionStatus status);
}
