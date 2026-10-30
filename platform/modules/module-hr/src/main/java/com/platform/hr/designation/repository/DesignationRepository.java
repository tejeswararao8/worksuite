package com.platform.hr.designation.repository;

import com.platform.hr.designation.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {

    Optional<Designation> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    List<Designation> findByCompanyIdAndIsActiveTrueOrderByLevelAsc(UUID companyId);
    boolean existsByTitleAndCompanyId(String title, UUID companyId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.designationId = :designationId AND e.isActive = true")
    long countActiveEmployeesByDesignationId(UUID designationId);

    @Query("SELECT d FROM Designation d WHERE d.companyId = :companyId AND d.isActive = true " +
           "AND (:query IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(d.grade) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Designation> search(UUID companyId, String query, Pageable pageable);
}
