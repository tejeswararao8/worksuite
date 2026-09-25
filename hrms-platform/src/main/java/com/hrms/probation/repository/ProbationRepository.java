package com.hrms.probation.repository;

import com.hrms.probation.entity.Probation;
import com.hrms.probation.entity.Probation.ProbationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProbationRepository extends JpaRepository<Probation, UUID> {
    List<Probation> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId);
    Optional<Probation> findByEmployeeIdAndCompanyIdAndStatus(UUID employeeId, UUID companyId, ProbationStatus status);
}
