package com.platform.hr.promotion.repository;

import com.platform.hr.promotion.entity.Promotion;
import com.platform.hr.promotion.entity.Promotion.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    Optional<Promotion> findByIdAndCompanyIdAndIsActiveTrue(UUID id, UUID companyId);
    Page<Promotion> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);
    Page<Promotion> findByCompanyIdAndStatusAndIsActiveTrue(UUID companyId, PromotionStatus status, Pageable pageable);
    Page<Promotion> findByEmployeeIdAndCompanyIdAndIsActiveTrue(UUID employeeId, UUID companyId, Pageable pageable);
    boolean existsByEmployeeIdAndStatusAndIsActiveTrue(UUID employeeId, PromotionStatus status);
}
