package com.platform.hr.bank.repository;

import com.platform.hr.bank.entity.BankDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankDetailRepository extends JpaRepository<BankDetail, UUID> {
    List<BankDetail> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId);
    Optional<BankDetail> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    Optional<BankDetail> findByEmployeeIdAndCompanyIdAndIsPrimaryTrue(UUID employeeId, UUID companyId);
}
