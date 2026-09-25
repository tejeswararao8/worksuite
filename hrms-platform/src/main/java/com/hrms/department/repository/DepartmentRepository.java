package com.hrms.department.repository;

import com.hrms.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    Page<Department> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);
    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}
