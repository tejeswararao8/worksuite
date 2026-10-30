package com.platform.hr.onboarding.repository;

import com.platform.hr.onboarding.entity.OnboardingChecklist;
import com.platform.hr.onboarding.entity.OnboardingChecklist.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingRepository extends JpaRepository<OnboardingChecklist, UUID> {

    List<OnboardingChecklist> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<OnboardingChecklist> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<OnboardingChecklist> findByAssignedToAndCompanyIdAndIsActiveTrue(UUID assignedTo, UUID companyId);
    long countByEmployeeIdAndCompanyIdAndStatusAndIsActiveTrue(UUID employeeId, UUID companyId, TaskStatus status);
    long countByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);

    @Query("SELECT t FROM OnboardingChecklist t WHERE t.companyId = :companyId AND t.isActive = true " +
           "AND t.status != 'COMPLETED' AND t.dueDate < :today")
    List<OnboardingChecklist> findOverdueTasks(UUID companyId, LocalDate today);

    @Query("SELECT DISTINCT t.employeeId FROM OnboardingChecklist t WHERE t.companyId = :companyId " +
           "AND t.isActive = true AND t.status != 'COMPLETED'")
    List<UUID> findEmployeesWithPendingTasks(UUID companyId);
}
