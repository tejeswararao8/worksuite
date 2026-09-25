package com.hrms.branch.repository;

import com.hrms.branch.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);

    Page<Branch> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}
