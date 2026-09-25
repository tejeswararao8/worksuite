package com.hrms.transfer.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.repository.EmployeeRepository;
import com.hrms.transfer.dto.TransferDto;
import com.hrms.transfer.entity.Transfer;
import com.hrms.transfer.entity.Transfer.TransferStatus;
import com.hrms.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final EmployeeRepository employeeRepository;

    public TransferDto.Response initiate(TransferDto.InitiateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Employee emp = findEmployee(req.getEmployeeId(), companyId);

        Transfer transfer = Transfer.builder()
                .employeeId(req.getEmployeeId())
                .transferType(req.getTransferType())
                .fromDepartmentId(emp.getDepartmentId())
                .toDepartmentId(req.getToDepartmentId())
                .fromBranchId(emp.getBranchId())
                .toBranchId(req.getToBranchId())
                .fromManagerId(emp.getManagerId())
                .toManagerId(req.getToManagerId())
                .fromTeamId(emp.getTeamId())
                .toTeamId(req.getToTeamId())
                .effectiveDate(req.getEffectiveDate())
                .reason(req.getReason())
                .status(TransferStatus.PENDING)
                .build();
        transfer.setCompanyId(companyId);
        return toResponse(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransferDto.Response> findAll(Pageable pageable) {
        return new PagedResponse<>(
                transferRepository.findByCompanyId(SecurityUtils.getCurrentCompanyId(), pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransferDto.Response> getHistory(UUID employeeId, Pageable pageable) {
        return new PagedResponse<>(
                transferRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId(), pageable)
                        .map(this::toResponse));
    }

    public TransferDto.Response approve(UUID id) {
        Transfer t = findPending(id);
        t.setStatus(TransferStatus.APPROVED);
        t.setApprovedBy(SecurityUtils.getCurrentUserId());
        t.setApprovedAt(LocalDateTime.now());
        applyToEmployee(t);
        t.setStatus(TransferStatus.COMPLETED);
        return toResponse(transferRepository.save(t));
    }

    public TransferDto.Response reject(UUID id, TransferDto.ActionRequest req) {
        Transfer t = findPending(id);
        t.setStatus(TransferStatus.REJECTED);
        t.setApprovedBy(SecurityUtils.getCurrentUserId());
        t.setApprovedAt(LocalDateTime.now());
        return toResponse(transferRepository.save(t));
    }

    private void applyToEmployee(Transfer t) {
        Employee emp = findEmployee(t.getEmployeeId(), t.getCompanyId());
        if (t.getToDepartmentId() != null) emp.setDepartmentId(t.getToDepartmentId());
        if (t.getToBranchId()     != null) emp.setBranchId(t.getToBranchId());
        if (t.getToManagerId()    != null) emp.setManagerId(t.getToManagerId());
        if (t.getToTeamId()       != null) emp.setTeamId(t.getToTeamId());
        employeeRepository.save(emp);
    }

    private Transfer findPending(UUID id) {
        Transfer t = transferRepository.findById(id)
                .filter(tr -> tr.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found: " + id));
        if (t.getStatus() != TransferStatus.PENDING)
            throw new BusinessException("INVALID_STATUS", "Transfer is not in PENDING state");
        return t;
    }

    private Employee findEmployee(UUID employeeId, UUID companyId) {
        return employeeRepository.findByIdAndCompanyIdAndActiveTrue(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private TransferDto.Response toResponse(Transfer t) {
        TransferDto.Response res = new TransferDto.Response();
        res.setId(t.getId());
        res.setEmployeeId(t.getEmployeeId());
        res.setTransferType(t.getTransferType());
        res.setFromDepartmentId(t.getFromDepartmentId());
        res.setToDepartmentId(t.getToDepartmentId());
        res.setFromBranchId(t.getFromBranchId());
        res.setToBranchId(t.getToBranchId());
        res.setFromManagerId(t.getFromManagerId());
        res.setToManagerId(t.getToManagerId());
        res.setFromTeamId(t.getFromTeamId());
        res.setToTeamId(t.getToTeamId());
        res.setEffectiveDate(t.getEffectiveDate());
        res.setReason(t.getReason());
        res.setStatus(t.getStatus());
        res.setApprovedBy(t.getApprovedBy());
        res.setApprovedAt(t.getApprovedAt());
        return res;
    }
}
