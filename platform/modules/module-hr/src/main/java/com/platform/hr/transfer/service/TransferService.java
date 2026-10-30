package com.platform.hr.transfer.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.branch.repository.BranchRepository;
import com.platform.hr.department.repository.DepartmentRepository;
import com.platform.hr.employee.entity.Employee;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.team.repository.TeamRepository;
import com.platform.hr.transfer.dto.TransferDto;
import com.platform.hr.transfer.entity.Transfer;
import com.platform.hr.transfer.entity.Transfer.TransferStatus;
import com.platform.hr.transfer.entity.Transfer.TransferType;
import com.platform.hr.transfer.repository.TransferRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.security.user.UserPrincipal;
import com.platform.tenancy.TenantContext;
import com.platform.workflow.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    @Auditable(entity = "Transfer", action = AuditLog.AuditAction.CREATE)
    public TransferDto.Response create(TransferDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Employee employee = employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getEmployeeId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));

        validateTransferTargets(request, companyId);

        Transfer transfer = Transfer.builder()
            .employeeId(request.getEmployeeId())
            .transferType(request.getTransferType())
            .fromDepartmentId(employee.getDepartmentId())
            .toDepartmentId(request.getToDepartmentId())
            .fromBranchId(employee.getBranchId())
            .toBranchId(request.getToBranchId())
            .fromManagerId(employee.getManagerId())
            .toManagerId(request.getToManagerId())
            .fromTeamId(employee.getTeamId())
            .toTeamId(request.getToTeamId())
            .effectiveDate(request.getEffectiveDate())
            .reason(request.getReason())
            .status(TransferStatus.PENDING)
            .build();
        transfer.setCompanyId(companyId);
        Transfer saved = transferRepository.save(transfer);

        // Start workflow approval
        try {
            var instance = workflowService.start("TRANSFER", saved.getId(),
                UserPrincipal.currentUserId(), companyId);
            saved.setApprovedBy(null);
            transferRepository.save(saved);
            log.info("Transfer workflow started: {}", instance.getId());
        } catch (Exception e) {
            log.warn("No workflow definition for TRANSFER — auto-approving: {}", e.getMessage());
            saved.setStatus(TransferStatus.APPROVED);
            saved.setApprovedAt(LocalDateTime.now());
            transferRepository.save(saved);
        }

        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransferDto.Response> findAll(TransferStatus status, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        if (status != null) {
            return PagedResponse.of(transferRepository
                .findByCompanyIdAndStatusAndIsActiveTrue(companyId, status, pageable)
                .map(this::enrichResponse));
        }
        return PagedResponse.of(transferRepository
            .findByCompanyIdAndIsActiveTrue(companyId, pageable).map(this::enrichResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransferDto.Response> findByEmployee(UUID employeeId, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(transferRepository
            .findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId, pageable)
            .map(this::enrichResponse));
    }

    @Transactional(readOnly = true)
    public TransferDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return enrichResponse(getActive(id, companyId));
    }

    @Auditable(entity = "Transfer", action = AuditLog.AuditAction.UPDATE)
    public TransferDto.Response approve(UUID id, TransferDto.ApproveRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Transfer transfer = getActive(id, companyId);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw PlatformException.badRequest("Transfer is already " + transfer.getStatus());
        }

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(UserPrincipal.currentUserId());
        transfer.setApprovedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(transfer.getEmployeeId())
            .title("Transfer Approved")
            .message("Your transfer request effective " + transfer.getEffectiveDate() + " has been approved.")
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(id.toString())
            .referenceType("TRANSFER")
            .build());

        // If effective date is today, apply immediately
        if (!transfer.getEffectiveDate().isAfter(LocalDate.now())) {
            applyTransfer(transfer);
        }

        return enrichResponse(transfer);
    }

    @Auditable(entity = "Transfer", action = AuditLog.AuditAction.UPDATE)
    public TransferDto.Response reject(UUID id, TransferDto.RejectRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Transfer transfer = getActive(id, companyId);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw PlatformException.badRequest("Transfer is already " + transfer.getStatus());
        }

        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setApprovedBy(UserPrincipal.currentUserId());
        transfer.setApprovedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(transfer.getEmployeeId())
            .title("Transfer Rejected")
            .message("Your transfer request has been rejected. Reason: " + request.getReason())
            .type(Notification.NotificationType.WORKFLOW_APPROVAL)
            .referenceId(id.toString())
            .referenceType("TRANSFER")
            .build());

        return enrichResponse(transfer);
    }

    // ── Scheduler: daily at 6am — apply approved transfers on effective date ──
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void applyEffectiveTransfers() {
        List<Transfer> transfers = transferRepository
            .findByStatusAndEffectiveDateAndIsActiveTrue(TransferStatus.APPROVED, LocalDate.now());
        for (Transfer transfer : transfers) {
            applyTransfer(transfer);
        }
        if (!transfers.isEmpty()) log.info("Applied {} transfers on effective date", transfers.size());
    }

    private void applyTransfer(Transfer transfer) {
        employeeRepository.findById(transfer.getEmployeeId()).ifPresent(employee -> {
            if (transfer.getTransferType() == TransferType.DEPARTMENT && transfer.getToDepartmentId() != null) {
                employee.setDepartmentId(transfer.getToDepartmentId());
            }
            if (transfer.getTransferType() == TransferType.BRANCH && transfer.getToBranchId() != null) {
                employee.setBranchId(transfer.getToBranchId());
            }
            if (transfer.getTransferType() == TransferType.MANAGER && transfer.getToManagerId() != null) {
                employee.setManagerId(transfer.getToManagerId());
            }
            if (transfer.getTransferType() == TransferType.TEAM && transfer.getToTeamId() != null) {
                employee.setTeamId(transfer.getToTeamId());
            }
            employeeRepository.save(employee);
            transfer.setStatus(TransferStatus.COMPLETED);
            transferRepository.save(transfer);
            log.info("Transfer applied for employee: {}", employee.getId());
        });
    }

    private void validateTransferTargets(TransferDto.CreateRequest request, UUID companyId) {
        if (request.getToDepartmentId() != null) {
            departmentRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getToDepartmentId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Target Department"));
        }
        if (request.getToBranchId() != null) {
            branchRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getToBranchId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Target Branch"));
        }
        if (request.getToManagerId() != null) {
            employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getToManagerId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Target Manager"));
        }
        if (request.getToTeamId() != null) {
            teamRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getToTeamId(), companyId)
                .orElseThrow(() -> PlatformException.notFound("Target Team"));
        }
    }

    private Transfer getActive(UUID id, UUID companyId) {
        return transferRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Transfer"));
    }

    private TransferDto.Response enrichResponse(Transfer t) {
        TransferDto.Response r = new TransferDto.Response();
        r.setId(t.getId()); r.setEmployeeId(t.getEmployeeId());
        r.setTransferType(t.getTransferType());
        r.setFromDepartmentId(t.getFromDepartmentId()); r.setToDepartmentId(t.getToDepartmentId());
        r.setFromBranchId(t.getFromBranchId()); r.setToBranchId(t.getToBranchId());
        r.setFromManagerId(t.getFromManagerId()); r.setToManagerId(t.getToManagerId());
        r.setFromTeamId(t.getFromTeamId()); r.setToTeamId(t.getToTeamId());
        r.setEffectiveDate(t.getEffectiveDate()); r.setReason(t.getReason());
        r.setStatus(t.getStatus()); r.setApprovedBy(t.getApprovedBy()); r.setApprovedAt(t.getApprovedAt());

        employeeRepository.findById(t.getEmployeeId()).ifPresent(e -> {
            r.setEmployeeName(e.getFirstName() + " " + e.getLastName());
            r.setEmployeeCode(e.getEmployeeCode());
        });
        if (t.getFromDepartmentId() != null)
            departmentRepository.findById(t.getFromDepartmentId()).ifPresent(d -> r.setFromDepartmentName(d.getName()));
        if (t.getToDepartmentId() != null)
            departmentRepository.findById(t.getToDepartmentId()).ifPresent(d -> r.setToDepartmentName(d.getName()));
        if (t.getFromBranchId() != null)
            branchRepository.findById(t.getFromBranchId()).ifPresent(b -> r.setFromBranchName(b.getName()));
        if (t.getToBranchId() != null)
            branchRepository.findById(t.getToBranchId()).ifPresent(b -> r.setToBranchName(b.getName()));
        if (t.getFromManagerId() != null)
            employeeRepository.findById(t.getFromManagerId()).ifPresent(e -> r.setFromManagerName(e.getFirstName() + " " + e.getLastName()));
        if (t.getToManagerId() != null)
            employeeRepository.findById(t.getToManagerId()).ifPresent(e -> r.setToManagerName(e.getFirstName() + " " + e.getLastName()));
        if (t.getFromTeamId() != null)
            teamRepository.findById(t.getFromTeamId()).ifPresent(tm -> r.setFromTeamName(tm.getName()));
        if (t.getToTeamId() != null)
            teamRepository.findById(t.getToTeamId()).ifPresent(tm -> r.setToTeamName(tm.getName()));
        return r;
    }
}
