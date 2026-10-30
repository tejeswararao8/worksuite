package com.platform.hr.offboarding.repository;

import com.platform.hr.offboarding.entity.OffboardingChecklist;
import com.platform.hr.offboarding.entity.OffboardingChecklist.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OffboardingRepository extends JpaRepository<OffboardingChecklist, UUID> {

    List<OffboardingChecklist> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<OffboardingChecklist> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<OffboardingChecklist> findByAssignedToAndCompanyIdAndIsActiveTrue(UUID assignedTo, UUID companyId);
    long countByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    long countByEmployeeIdAndCompanyIdAndStatusAndIsActiveTrue(UUID employeeId, UUID companyId, TaskStatus status);

    @Query("SELECT t FROM OffboardingChecklist t WHERE t.companyId = :companyId AND t.isActive = true " +
           "AND t.status != 'COMPLETED' AND t.dueDate < :today")
    List<OffboardingChecklist> findOverdueTasks(UUID companyId, LocalDate today);

    @Query("SELECT DISTINCT t.employeeId FROM OffboardingChecklist t WHERE t.companyId = :companyId " +
           "AND t.isActive = true")
    List<UUID> findEmployeesInOffboarding(UUID companyId);
}
