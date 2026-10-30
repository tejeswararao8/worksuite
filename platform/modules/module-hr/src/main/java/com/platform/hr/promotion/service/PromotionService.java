package com.platform.hr.promotion.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.designation.repository.DesignationRepository;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.promotion.dto.PromotionDto;
import com.platform.hr.promotion.entity.Promotion;
import com.platform.hr.promotion.entity.Promotion.PromotionStatus;
import com.platform.hr.promotion.repository.PromotionRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantContext;
import com.platform.workflow.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    @Auditable(entity = "Promotion", action = AuditLog.AuditAction.CREATE)
    public PromotionDto.Response create(PromotionDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getEmployeeId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));

        // Cannot have two pending promotions
        if (promotionRepository.existsByEmployeeIdAndStatusAndIsActiveTrue(
                request.getEmployeeId(), PromotionStatus.PENDING)) {
            throw PlatformException.conflict("Employee already has a pending promotion request");
        }

        // Validate designation level — cannot promote to same or lower level
        var toDesignation = designationRepository.findByIdAndCompanyIdAndIsActiveTrue(
                request.getToDesignationId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Target Designation"));

        if (employee.getDesignationId() != null) {
            designationRepository.findById(employee.getDesignationId()).ifPresent(fromDesig -> {
                if (fromDesig.getLevel() != null && toDesignation.getLevel() != null
                        && toDesignation.getLevel() <= fromDesig.getLevel()) {
                    throw PlatformException.badRequest(
                        "Cannot promote to same or lower designation level. Current: "
                        + fromDesig.getLevel() + ", Target: " + toDesignation.getLevel());
                }
            });
        }

        Promotion promotion = Promotion.builder()
            .employeeId(request.getEmployeeId())
            .fromDesignationId(employee.getDesignationId())
            .toDesignationId(request.getToDesignationId())
            .effectiveDate(request.getEffectiveDate())
            .reason(request.getReason())
            .status(PromotionStatus.PENDING)
            .build();
        promotion.setCompanyId(companyId);
        Promotion saved = promotionRepository.save(promotion);

        // Start workflow
        try {
            workflowService.start("PROMOTION", saved.getId(), UserPrincipal.currentUserId(), companyId);
        } catch (Exception e) {
            log.warn("No workflow definition for PROMOTION — auto-approving: {}", e.getMessage());
            saved.setStatus(PromotionStatus.APPROVED);
            saved.setApprovedAt(LocalDateTime.now());
            promotionRepository.save(saved);
            applyPromotion(saved, employee);
        }

        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PromotionDto.Response> findAll(PromotionStatus status, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        if (status != null) {
            return PagedResponse.of(promotionRepository
                .findByCompanyIdAndStatusAndIsActiveTrue(companyId, status, pageable)
                .map(this::enrichResponse));
        }
        return PagedResponse.of(promotionRepository
            .findByCompanyIdAndIsActiveTrue(companyId, pageable).map(this::enrichResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PromotionDto.Response> findByEmployee(UUID employeeId, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(promotionRepository
            .findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId, pageable)
            .map(this::enrichResponse));
    }

    @Auditable(entity = "Promotion", action = AuditLog.AuditAction.UPDATE)
    public PromotionDto.Response approve(UUID id, PromotionDto.ApproveRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Promotion promotion = getActive(id, companyId);

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw PlatformException.badRequest("Promotion is already " + promotion.getStatus());
        }

        promotion.setStatus(PromotionStatus.APPROVED);
        promotion.setApprovedBy(UserPrincipal.currentUserId());
        promotion.setApprovedAt(LocalDateTime.now());
        promotionRepository.save(promotion);

        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(
            promotion.getEmployeeId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
        applyPromotion(promotion, employee);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(promotion.getEmployeeId())
            .title("Promotion Approved")
            .message("Congratulations! Your promotion has been approved effective " + promotion.getEffectiveDate())
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(id.toString())
            .referenceType("PROMOTION")
            .build());

        return enrichResponse(promotion);
    }

    @Auditable(entity = "Promotion", action = AuditLog.AuditAction.UPDATE)
    public PromotionDto.Response reject(UUID id, PromotionDto.RejectRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Promotion promotion = getActive(id, companyId);

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw PlatformException.badRequest("Promotion is already " + promotion.getStatus());
        }

        promotion.setStatus(PromotionStatus.REJECTED);
        promotion.setApprovedBy(UserPrincipal.currentUserId());
        promotion.setApprovedAt(LocalDateTime.now());
        promotionRepository.save(promotion);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(promotion.getEmployeeId())
            .title("Promotion Rejected")
            .message("Your promotion request has been rejected. Reason: " + request.getReason())
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(id.toString())
            .referenceType("PROMOTION")
            .build());

        return enrichResponse(promotion);
    }

    private void applyPromotion(Promotion promotion, Employee employee) {
        employee.setDesignationId(promotion.getToDesignationId());
        employeeRepository.save(employee);
        log.info("Promotion applied for employee: {}", employee.getId());
    }

    private Promotion getActive(UUID id, UUID companyId) {
        return promotionRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Promotion"));
    }

    private PromotionDto.Response enrichResponse(Promotion p) {
        PromotionDto.Response r = new PromotionDto.Response();
        r.setId(p.getId()); r.setEmployeeId(p.getEmployeeId());
        r.setFromDesignationId(p.getFromDesignationId()); r.setToDesignationId(p.getToDesignationId());
        r.setEffectiveDate(p.getEffectiveDate()); r.setReason(p.getReason());
        r.setStatus(p.getStatus()); r.setApprovedBy(p.getApprovedBy()); r.setApprovedAt(p.getApprovedAt());

        employeeRepository.findById(p.getEmployeeId()).ifPresent(e -> {
            r.setEmployeeName(e.getFirstName() + " " + e.getLastName());
            r.setEmployeeCode(e.getEmployeeCode());
        });
        if (p.getFromDesignationId() != null)
            designationRepository.findById(p.getFromDesignationId())
                .ifPresent(d -> r.setFromDesignationTitle(d.getTitle()));
        if (p.getToDesignationId() != null)
            designationRepository.findById(p.getToDesignationId())
                .ifPresent(d -> r.setToDesignationTitle(d.getTitle()));
        return r;
    }
}
