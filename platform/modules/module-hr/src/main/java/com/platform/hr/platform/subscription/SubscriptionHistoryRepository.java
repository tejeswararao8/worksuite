package com.platform.hr.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, UUID> {

    List<SubscriptionHistory> findByTenantIdOrderByOccurredAtDesc(UUID tenantId);

    List<SubscriptionHistory> findBySubscriptionIdOrderByOccurredAtDesc(UUID subscriptionId);
}
