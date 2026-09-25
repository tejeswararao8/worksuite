package com.hrms.promotion.repository;

import com.hrms.promotion.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Page<Promotion> findByEmployeeIdAndCompanyId(UUID employeeId, UUID companyId, Pageable pageable);
    Page<Promotion> findByCompanyId(UUID companyId, Pageable pageable);
}
