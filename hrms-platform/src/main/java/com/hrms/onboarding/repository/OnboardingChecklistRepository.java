package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.OnboardingChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OnboardingChecklistRepository extends JpaRepository<OnboardingChecklist, UUID> {
    List<OnboardingChecklist> findByEmployeeIdAndCompanyIdOrderBySequenceOrder(UUID employeeId, UUID companyId);
    boolean existsByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
}
