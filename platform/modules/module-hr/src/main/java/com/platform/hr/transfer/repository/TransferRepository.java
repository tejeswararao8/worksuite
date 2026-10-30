package com.platform.hr.transfer.repository;

import com.platform.hr.transfer.entity.Transfer;
import com.platform.hr.transfer.entity.Transfer.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    Page<Transfer> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);
    Page<Transfer> findByCompanyIdAndStatusAndIsActiveTrue(UUID companyId, TransferStatus status, Pageable pageable);
    Page<Transfer> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId, Pageable pageable);
    List<Transfer> findByStatusAndEffectiveDateAndIsActiveTrue(TransferStatus status, LocalDate effectiveDate);
}
