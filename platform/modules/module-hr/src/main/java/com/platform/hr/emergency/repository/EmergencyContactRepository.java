package com.platform.hr.emergency.repository;

import com.platform.hr.emergency.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
    List<EmergencyContact> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<EmergencyContact> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    long countByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
}
