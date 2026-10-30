package com.platform.hr.skills.service;

import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.hr.skills.dto.SkillDto;
import com.platform.hr.skills.entity.EmployeeCertification;
import com.platform.hr.skills.entity.EmployeeSkill;
import com.platform.hr.skills.repository.CertificationRepository;
import com.platform.hr.skills.repository.SkillRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    // ── SKILLS ────────────────────────────────────────────────

    @Auditable(entity = "EmployeeSkill", action = AuditLog.AuditAction.CREATE)
    public SkillDto.SkillResponse addSkill(UUID employeeId, SkillDto.CreateSkillRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        EmployeeSkill skill = EmployeeSkill.builder()
            .employeeId(employeeId)
            .skillName(request.getSkillName())
            .skillType(request.getSkillType())
            .proficiency(request.getProficiency())
            .yearsOfExperience(request.getYearsOfExperience())
            .build();
        skill.setCompanyId(companyId);
        return toSkillResponse(skillRepository.save(skill));
    }

    @Transactional(readOnly = true)
    public List<SkillDto.SkillResponse> findSkillsByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return skillRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toSkillResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDto.SkillResponse> searchBySkillName(String skillName) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return skillRepository.findBySkillName(companyId, skillName)
            .stream().map(this::toSkillResponse).toList();
    }

    @Auditable(entity = "EmployeeSkill", action = AuditLog.AuditAction.UPDATE)
    public SkillDto.SkillResponse updateSkill(UUID employeeId, UUID skillId, SkillDto.UpdateSkillRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmployeeSkill skill = skillRepository.findByIdAndCompanyIdAndIsActiveTrue(skillId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Skill"));
        if (!skill.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Skill does not belong to this employee");
        }
        if (request.getSkillName() != null) skill.setSkillName(request.getSkillName());
        if (request.getSkillType() != null) skill.setSkillType(request.getSkillType());
        if (request.getProficiency() != null) skill.setProficiency(request.getProficiency());
        if (request.getYearsOfExperience() != null) skill.setYearsOfExperience(request.getYearsOfExperience());
        return toSkillResponse(skillRepository.save(skill));
    }

    @Auditable(entity = "EmployeeSkill", action = AuditLog.AuditAction.DELETE)
    public void deleteSkill(UUID employeeId, UUID skillId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmployeeSkill skill = skillRepository.findByIdAndCompanyIdAndIsActiveTrue(skillId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Skill"));
        if (!skill.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Skill does not belong to this employee");
        }
        skill.setIsActive(false);
        skillRepository.save(skill);
    }

    // ── CERTIFICATIONS ────────────────────────────────────────

    @Auditable(entity = "EmployeeCertification", action = AuditLog.AuditAction.CREATE)
    public SkillDto.CertificationResponse addCertification(UUID employeeId, SkillDto.CreateCertificationRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        validateEmployee(employeeId, companyId);

        EmployeeCertification cert = EmployeeCertification.builder()
            .employeeId(employeeId)
            .name(request.getName())
            .issuingOrg(request.getIssuingOrg())
            .issueDate(request.getIssueDate())
            .expiryDate(request.getExpiryDate())
            .credentialUrl(request.getCredentialUrl())
            .build();
        cert.setCompanyId(companyId);
        return toCertResponse(certificationRepository.save(cert));
    }

    @Transactional(readOnly = true)
    public List<SkillDto.CertificationResponse> findCertificationsByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return certificationRepository.findByEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toCertResponse).toList();
    }

    @Auditable(entity = "EmployeeCertification", action = AuditLog.AuditAction.UPDATE)
    public SkillDto.CertificationResponse updateCertification(UUID employeeId, UUID certId,
                                                               SkillDto.UpdateCertificationRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmployeeCertification cert = certificationRepository.findByIdAndCompanyIdAndIsActiveTrue(certId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Certification"));
        if (!cert.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Certification does not belong to this employee");
        }
        if (request.getName() != null) cert.setName(request.getName());
        if (request.getIssuingOrg() != null) cert.setIssuingOrg(request.getIssuingOrg());
        if (request.getIssueDate() != null) cert.setIssueDate(request.getIssueDate());
        if (request.getExpiryDate() != null) cert.setExpiryDate(request.getExpiryDate());
        if (request.getCredentialUrl() != null) cert.setCredentialUrl(request.getCredentialUrl());
        return toCertResponse(certificationRepository.save(cert));
    }

    @Auditable(entity = "EmployeeCertification", action = AuditLog.AuditAction.DELETE)
    public void deleteCertification(UUID employeeId, UUID certId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        EmployeeCertification cert = certificationRepository.findByIdAndCompanyIdAndIsActiveTrue(certId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Certification"));
        if (!cert.getEmployeeId().equals(employeeId)) {
            throw PlatformException.forbidden("Certification does not belong to this employee");
        }
        cert.setIsActive(false);
        certificationRepository.save(cert);
    }

    // ── Scheduler: daily at 8am — certification expiry reminders ──
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendCertificationExpiryReminders() {
        List<UUID> companyIds = certificationRepository.findAll().stream()
            .map(c -> c.getCompanyId()).distinct().toList();

        for (UUID companyId : companyIds) {
            LocalDate today = LocalDate.now();
            for (int days : new int[]{30, 7}) {
                LocalDate target = today.plusDays(days);
                List<EmployeeCertification> expiring = certificationRepository
                    .findExpiringBetween(companyId, target, target);
                for (EmployeeCertification cert : expiring) {
                    notificationService.send(NotificationRequest.builder()
                        .companyId(companyId)
                        .recipientId(cert.getEmployeeId())
                        .title("Certification Expiring in " + days + " Days")
                        .message("Your certification '" + cert.getName() + "' expires on " + cert.getExpiryDate())
                        .type(Notification.NotificationType.DOCUMENT_EXPIRY)
                        .referenceId(cert.getId().toString())
                        .referenceType("CERTIFICATION")
                        .build());
                }
            }
        }
    }

    private void validateEmployee(UUID employeeId, UUID companyId) {
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));
    }

    private SkillDto.SkillResponse toSkillResponse(EmployeeSkill s) {
        SkillDto.SkillResponse r = new SkillDto.SkillResponse();
        r.setId(s.getId()); r.setEmployeeId(s.getEmployeeId());
        r.setSkillName(s.getSkillName()); r.setSkillType(s.getSkillType());
        r.setProficiency(s.getProficiency()); r.setYearsOfExperience(s.getYearsOfExperience());
        return r;
    }

    private SkillDto.CertificationResponse toCertResponse(EmployeeCertification c) {
        SkillDto.CertificationResponse r = new SkillDto.CertificationResponse();
        r.setId(c.getId()); r.setEmployeeId(c.getEmployeeId());
        r.setName(c.getName()); r.setIssuingOrg(c.getIssuingOrg());
        r.setIssueDate(c.getIssueDate()); r.setExpiryDate(c.getExpiryDate());
        r.setCredentialUrl(c.getCredentialUrl());
        r.setExpired(c.getExpiryDate() != null && c.getExpiryDate().isBefore(LocalDate.now()));
        r.setExpiringSoon(c.getExpiryDate() != null
            && !c.getExpiryDate().isBefore(LocalDate.now())
            && c.getExpiryDate().isBefore(LocalDate.now().plusDays(30)));
        return r;
    }
}
