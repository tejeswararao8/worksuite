package com.hrms.employee.repository;

import com.hrms.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    List<Employee> findByCompanyIdAndActiveTrue(UUID companyId);
    boolean existsByEmailAndCompanyId(String email, UUID companyId);
    boolean existsByEmployeeCodeAndCompanyId(String code, UUID companyId);
    long countByCompanyIdAndActiveTrue(UUID companyId);
    long countByCompanyIdAndEmploymentStatusAndActiveTrue(UUID companyId, Employee.EmploymentStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.employeeCode, 4) AS int)), 0) FROM Employee e WHERE e.companyId = :companyId")
    int findMaxEmployeeCodeSequence(UUID companyId);

    @Query("SELECT CAST(e.departmentId AS string) AS departmentId, COUNT(e) AS count FROM Employee e WHERE e.companyId = :companyId AND e.active = true GROUP BY e.departmentId")
    List<Map<String, Object>> countByDepartmentAndCompanyId(UUID companyId);

    @Query("SELECT CAST(e.branchId AS string) AS branchId, COUNT(e) AS count FROM Employee e WHERE e.companyId = :companyId AND e.active = true GROUP BY e.branchId")
    List<Map<String, Object>> countByBranchAndCompanyId(UUID companyId);

    @Query("SELECT FUNCTION('TO_CHAR', e.joiningDate, 'YYYY-MM') AS month, COUNT(e) AS count FROM Employee e WHERE e.companyId = :companyId AND e.joiningDate >= :from GROUP BY FUNCTION('TO_CHAR', e.joiningDate, 'YYYY-MM') ORDER BY month")
    List<Map<String, Object>> joiningTrend(UUID companyId, LocalDate from);
}
