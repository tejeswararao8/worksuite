package com.platform.hr.skills.repository;

import com.platform.hr.skills.entity.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificationRepository extends JpaRepository<EmployeeCertification, UUID> {
    List<EmployeeCertification> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<EmployeeCertification> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);

    @Query("SELECT c FROM EmployeeCertification c WHERE c.companyId = :companyId AND c.isActive = true " +
           "AND c.expiryDate IS NOT NULL AND c.expiryDate BETWEEN :from AND :to")
    List<EmployeeCertification> findExpiringBetween(UUID companyId, LocalDate from, LocalDate to);
}
