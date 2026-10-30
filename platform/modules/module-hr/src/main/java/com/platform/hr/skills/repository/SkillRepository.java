package com.platform.hr.skills.repository;

import com.platform.hr.skills.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<EmployeeSkill, UUID> {
    List<EmployeeSkill> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<EmployeeSkill> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);

    @Query("SELECT s FROM EmployeeSkill s WHERE s.companyId = :companyId AND s.isActive = true " +
           "AND LOWER(s.skillName) LIKE LOWER(CONCAT('%', :skillName, '%'))")
    List<EmployeeSkill> findBySkillName(UUID companyId, String skillName);
}
