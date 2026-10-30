package com.platform.hr.team.repository;

import com.platform.hr.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<Team> findByCompanyIdAndIsActiveTrue(UUID companyId);
    List<Team> findByDepartmentIdAndCompanyIdAndIsActiveTrue(UUID departmentId, UUID companyId);
    boolean existsByNameAndDepartmentIdAndCompanyId(String name, UUID departmentId, UUID companyId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.teamId = :teamId AND e.isActive = true")
    long countActiveEmployeesByTeamId(UUID teamId);

    @Query("SELECT t FROM Team t WHERE t.companyId = :companyId AND t.isActive = true " +
           "AND (:query IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:departmentId IS NULL OR t.departmentId = :departmentId)")
    Page<Team> search(UUID companyId, String query, UUID departmentId, Pageable pageable);
}
