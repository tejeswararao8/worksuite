package com.hrms.offboarding.repository;

import com.hrms.offboarding.entity.OffboardingChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OffboardingChecklistRepository extends JpaRepository<OffboardingChecklist, UUID> {
    List<OffboardingChecklist> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
    boolean existsByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
}
