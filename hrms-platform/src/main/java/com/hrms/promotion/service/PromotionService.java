package com.hrms.promotion.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.repository.EmployeeRepository;
import com.hrms.promotion.dto.PromotionDto;
import com.hrms.promotion.entity.Promotion;
import com.hrms.promotion.entity.Promotion.PromotionStatus;
import com.hrms.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final EmployeeRepository employeeRepository;

    public PromotionDto.Response initiate(PromotionDto.InitiateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Employee emp = findEmployee(req.getEmployeeId(), companyId);

        Promotion promotion = Promotion.builder()
                .employeeId(req.getEmployeeId())
                .fromDesignationId(emp.getDesignationId())
                .toDesignationId(req.getToDesignationId())
                .effectiveDate(req.getEffectiveDate())
                .reason(req.getReason())
                .status(PromotionStatus.PENDING)
                .build();
        promotion.setCompanyId(companyId);
        return toResponse(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PromotionDto.Response> findAll(Pageable pageable) {
        return new PagedResponse<>(
                promotionRepository.findByCompanyId(SecurityUtils.getCurrentCompanyId(), pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PromotionDto.Response> getHistory(UUID employeeId, Pageable pageable) {
        return new PagedResponse<>(
                promotionRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId(), pageable)
                        .map(this::toResponse));
    }

    public PromotionDto.Response approve(UUID id) {
        Promotion p = findPending(id);
        p.setStatus(PromotionStatus.APPROVED);
        p.setApprovedBy(SecurityUtils.getCurrentUserId());
        p.setApprovedAt(LocalDateTime.now());
        Employee emp = findEmployee(p.getEmployeeId(), p.getCompanyId());
        emp.setDesignationId(p.getToDesignationId());
        employeeRepository.save(emp);
        return toResponse(promotionRepository.save(p));
    }

    public PromotionDto.Response reject(UUID id, PromotionDto.ActionRequest req) {
        Promotion p = findPending(id);
        p.setStatus(PromotionStatus.REJECTED);
        p.setApprovedBy(SecurityUtils.getCurrentUserId());
        p.setApprovedAt(LocalDateTime.now());
        return toResponse(promotionRepository.save(p));
    }

    private Promotion findPending(UUID id) {
        Promotion p = promotionRepository.findById(id)
                .filter(pr -> pr.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + id));
        if (p.getStatus() != PromotionStatus.PENDING)
            throw new BusinessException("INVALID_STATUS", "Promotion is not in PENDING state");
        return p;
    }

    private Employee findEmployee(UUID employeeId, UUID companyId) {
        return employeeRepository.findByIdAndCompanyIdAndActiveTrue(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private PromotionDto.Response toResponse(Promotion p) {
        PromotionDto.Response res = new PromotionDto.Response();
        res.setId(p.getId());
        res.setEmployeeId(p.getEmployeeId());
        res.setFromDesignationId(p.getFromDesignationId());
        res.setToDesignationId(p.getToDesignationId());
        res.setEffectiveDate(p.getEffectiveDate());
        res.setReason(p.getReason());
        res.setStatus(p.getStatus());
        res.setApprovedBy(p.getApprovedBy());
        res.setApprovedAt(p.getApprovedAt());
        return res;
    }
}
