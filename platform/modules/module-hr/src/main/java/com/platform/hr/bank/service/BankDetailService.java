package com.platform.hr.bank.service;

import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.bank.dto.BankDetailDto;
import com.platform.hr.bank.entity.BankDetail;
import com.platform.hr.bank.repository.BankDetailRepository;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BankDetailService {

    private final BankDetailRepository bankDetailRepository;
    private final EmployeeRepository employeeRepository;

    @Auditable(entity = "BankDetail", action = AuditLog.AuditAction.CREATE)
    public BankDetailDto.Response create(UUID employeeId, BankDetailDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryFlag(employeeId, companyId);
        }

        BankDetail bank = BankDetail.builder()
            .employeeId(employeeId)
            .bankName(request.getBankName())
            .accountHolderName(request.getAccountHolderName())
            .accountNumber(request.getAccountNumber())
            .iban(request.getIban())
            .swiftCode(request.getSwiftCode())
            .branchName(request.getBranchName())
            .branchAddress(request.getBranchAddress())
            .currency(request.getCurrency() != null ? request.getCurrency() : "AED")
            .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
            .build();
        bank.setCompanyId(companyId);
        return toResponse(bankDetailRepository.save(bank));
    }

    @Transactional(readOnly = true)
    public List<BankDetailDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);
        return bankDetailRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Auditable(entity = "BankDetail", action = AuditLog.AuditAction.UPDATE)
    public BankDetailDto.Response update(UUID employeeId, UUID bankId, BankDetailDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        BankDetail bank = bankDetailRepository.findByIdAndCompanyIdAndIsActiveTrue(bankId, companyId)
            .orElseThrow(() -> PlatformException.notFound("BankDetail"));
        if (!bank.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Bank detail does not belong to this employee");
        }
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryFlag(employeeId, companyId);
        }
        if (request.getBankName() != null) bank.setBankName(request.getBankName());
        if (request.getAccountHolderName() != null) bank.setAccountHolderName(request.getAccountHolderName());
        if (request.getAccountNumber() != null) bank.setAccountNumber(request.getAccountNumber());
        if (request.getIban() != null) bank.setIban(request.getIban());
        if (request.getSwiftCode() != null) bank.setSwiftCode(request.getSwiftCode());
        if (request.getBranchName() != null) bank.setBranchName(request.getBranchName());
        if (request.getCurrency() != null) bank.setCurrency(request.getCurrency());
        if (request.getIsPrimary() != null) bank.setIsPrimary(request.getIsPrimary());
        return toResponse(bankDetailRepository.save(bank));
    }

    @Auditable(entity = "BankDetail", action = AuditLog.AuditAction.DELETE)
    public void delete(UUID employeeId, UUID bankId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        BankDetail bank = bankDetailRepository.findByIdAndCompanyIdAndIsActiveTrue(bankId, companyId)
            .orElseThrow(() -> PlatformException.notFound("BankDetail"));
        if (!bank.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Bank detail does not belong to this employee");
        }
        bank.setIsActive(false);
        bankDetailRepository.save(bank);
    }

    private void clearPrimaryFlag(UUID employeeId, UUID companyId) {
        bankDetailRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .forEach(b -> { b.setIsPrimary(false); bankDetailRepository.save(b); });
    }

    private void validateEmployee(UUID employeeId, UUID companyId) {
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private BankDetailDto.Response toResponse(BankDetail b) {
        BankDetailDto.Response r = new BankDetailDto.Response();
        r.setId(b.getId()); r.setEmployeeId(b.getEmployeeId());
        r.setBankName(b.getBankName()); r.setAccountHolderName(b.getAccountHolderName());
        r.setAccountNumber(b.getAccountNumber()); r.setIban(b.getIban());
        r.setSwiftCode(b.getSwiftCode()); r.setBranchName(b.getBranchName());
        r.setCurrency(b.getCurrency()); r.setIsPrimary(b.getIsPrimary());
        return r;
    }
}
