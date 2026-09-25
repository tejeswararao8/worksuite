package com.hrms.skills.repository;

import com.hrms.skills.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, UUID> {
    List<EmployeeSkill> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
}
