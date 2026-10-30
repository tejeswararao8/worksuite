package com.platform.hr.employee.repository;

import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.entity.Employee.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmailAndCompanyId(String email, UUID companyId);
    Optional<Employee> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    long countByCompanyIdAndEmploymentStatus(UUID companyId, EmploymentStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.employeeCode, 4) AS int)), 0) FROM Employee e WHERE e.companyId = :companyId")
    int findMaxEmployeeCodeSequence(UUID companyId);
}
