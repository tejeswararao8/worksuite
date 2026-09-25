package com.hrms.team.repository;

import com.hrms.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    Page<Team> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);
    Page<Team> findByDepartmentIdAndCompanyIdAndActiveTrue(UUID departmentId, UUID companyId, Pageable pageable);
}
