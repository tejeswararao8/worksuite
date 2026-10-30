package com.platform.hr.branch.repository;

import com.platform.hr.branch.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    Page<Branch> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);
    List<Branch> findByCompanyIdAndIsActiveTrue(UUID companyId);
    boolean existsByCodeAndCompanyId(String code, UUID companyId);
    boolean existsByNameAndCompanyId(String name, UUID companyId);
    long countByCompanyIdAndIsActiveTrue(UUID companyId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.branchId = :branchId AND e.isActive = true")
    long countActiveEmployeesByBranchId(UUID branchId);

    @Query("SELECT b FROM Branch b WHERE b.companyId = :companyId AND b.isActive = true " +
           "AND (:query IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Branch> search(UUID companyId, String query, Pageable pageable);
}
