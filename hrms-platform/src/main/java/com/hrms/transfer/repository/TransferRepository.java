package com.hrms.transfer.repository;

import com.hrms.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Page<Transfer> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId, Pageable pageable);
    Page<Transfer> findByCompanyId(UUID companyId, Pageable pageable);
}
