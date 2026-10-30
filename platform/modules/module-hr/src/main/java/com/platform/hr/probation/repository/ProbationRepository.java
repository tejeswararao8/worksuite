package com.platform.hr.probation.repository;

import com.platform.hr.probation.entity.Probation;
import com.platform.hr.probation.entity.Probation.ProbationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProbationRepository extends JpaRepository<Probation, UUID> {

    Optional<Probation> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    Optional<Probation> findByEmployeeIdAndStatusAndIsActiveTrue(UUID employeeId, ProbationStatus status);
    Page<Probation> findByCompanyIdAndStatusAndIsActiveTrue(UUID companyId, ProbationStatus status, Pageable pageable);
    Page<Probation> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);

    @Query("SELECT p FROM Probation p WHERE p.companyId = :companyId AND p.isActive = true " +
           "AND p.status = 'IN_PROGRESS' " +
           "AND (p.extendedEndDate IS NOT NULL AND p.extendedEndDate BETWEEN :from AND :to " +
           "OR p.extendedEndDate IS NULL AND p.endDate BETWEEN :from AND :to)")
    List<Probation> findEndingBetween(UUID companyId, LocalDate from, LocalDate to);
}
