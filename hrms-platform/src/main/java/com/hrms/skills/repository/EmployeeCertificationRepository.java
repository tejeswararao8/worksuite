package com.hrms.skills.repository;

import com.hrms.skills.entity.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, UUID> {
    List<EmployeeCertification> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
}
