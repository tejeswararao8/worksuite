package com.platform.hr.department.repository;

import com.platform.hr.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<Department> findByCompanyIdAndIsActiveTrue(UUID companyId);
    boolean existsByCodeAndCompanyId(String code, UUID companyId);
    boolean existsByNameAndCompanyId(String name, UUID companyId);

    List<Department> findByParentDepartmentIdAndCompanyIdAndIsActiveTrue(UUID parentId, UUID companyId);
    List<Department> findByParentDepartmentIdIsNullAndCompanyIdAndIsActiveTrue(UUID companyId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.departmentId = :departmentId AND e.isActive = true")
    long countActiveEmployeesByDepartmentId(UUID departmentId);

    @Query("SELECT COUNT(t) FROM Team t WHERE t.departmentId = :departmentId AND t.isActive = true")
    long countActiveTeamsByDepartmentId(UUID departmentId);

    @Query("SELECT d FROM Department d WHERE d.companyId = :companyId AND d.isActive = true " +
           "AND (:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Department> search(UUID companyId, String query, Pageable pageable);
}
