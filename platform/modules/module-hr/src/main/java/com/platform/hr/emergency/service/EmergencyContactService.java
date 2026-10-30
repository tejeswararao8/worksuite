package com.platform.hr.emergency.service;

import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.emergency.dto.EmergencyContactDto;
import com.platform.hr.emergency.entity.EmergencyContact;
import com.platform.hr.emergency.repository.EmergencyContactRepository;
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
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final EmployeeRepository employeeRepository;

    @Auditable(entity = "EmergencyContact", action = AuditLog.AuditAction.CREATE)
    public EmergencyContactDto.Response create(UUID employeeId, EmergencyContactDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        // Only one primary contact allowed
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryFlag(employeeId, companyId);
        }

        EmergencyContact contact = EmergencyContact.builder()
            .employeeId(employeeId)
            .name(request.getName())
            .relationship(request.getRelationship())
            .phone(request.getPhone())
            .alternatePhone(request.getAlternatePhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
            .build();
        contact.setCompanyId(companyId);
        return toResponse(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public List<EmergencyContactDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);
        return contactRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Auditable(entity = "EmergencyContact", action = AuditLog.AuditAction.UPDATE)
    public EmergencyContactDto.Response update(UUID employeeId, UUID contactId, EmergencyContactDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmergencyContact contact = contactRepository.findByIdAndCompanyIdAndIsActiveTrue(contactId, companyId)
            .orElseThrow(() -> PlatformException.notFound("EmergencyContact"));

        if (!contact.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Contact does not belong to this employee");
        }
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryFlag(employeeId, companyId);
        }

        if (request.getName() != null) contact.setName(request.getName());
        if (request.getRelationship() != null) contact.setRelationship(request.getRelationship());
        if (request.getPhone() != null) contact.setPhone(request.getPhone());
        if (request.getAlternatePhone() != null) contact.setAlternatePhone(request.getAlternatePhone());
        if (request.getEmail() != null) contact.setEmail(request.getEmail());
        if (request.getAddress() != null) contact.setAddress(request.getAddress());
        if (request.getIsPrimary() != null) contact.setIsPrimary(request.getIsPrimary());

        return toResponse(contactRepository.save(contact));
    }

    @Auditable(entity = "EmergencyContact", action = AuditLog.AuditAction.DELETE)
    public void delete(UUID employeeId, UUID contactId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmergencyContact contact = contactRepository.findByIdAndCompanyIdAndIsActiveTrue(contactId, companyId)
            .orElseThrow(() -> PlatformException.notFound("EmergencyContact"));
        if (!contact.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Contact does not belong to this employee");
        }
        contact.setIsActive(false);
        contactRepository.save(contact);
    }

    private void clearPrimaryFlag(UUID employeeId, UUID companyId) {
        contactRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .forEach(c -> { c.setIsPrimary(false); contactRepository.save(c); });
    }

    private void validateEmployee(UUID employeeId, UUID companyId) {
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private EmergencyContactDto.Response toResponse(EmergencyContact c) {
        EmergencyContactDto.Response r = new EmergencyContactDto.Response();
        r.setId(c.getId()); r.setEmployeeId(c.getEmployeeId());
        r.setName(c.getName()); r.setRelationship(c.getRelationship());
        r.setPhone(c.getPhone()); r.setAlternatePhone(c.getAlternatePhone());
        r.setEmail(c.getEmail()); r.setAddress(c.getAddress());
        r.setIsPrimary(c.getIsPrimary());
        return r;
    }
}
