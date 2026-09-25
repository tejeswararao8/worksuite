package com.hrms.designation.repository;

import com.hrms.designation.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    Optional<Designation> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    Page<Designation> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);
}
