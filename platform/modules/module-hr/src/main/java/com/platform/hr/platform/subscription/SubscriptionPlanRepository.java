package com.platform.hr.platform.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Page<SubscriptionPlan> findByPlanStatus(SubscriptionPlan.PlanStatus status, Pageable pageable);

    boolean existsByName(String name);
}
