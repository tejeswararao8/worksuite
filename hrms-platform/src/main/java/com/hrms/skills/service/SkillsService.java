package com.hrms.skills.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.skills.dto.SkillsDto;
import com.hrms.skills.entity.EmployeeCertification;
import com.hrms.skills.entity.EmployeeSkill;
import com.hrms.skills.repository.EmployeeCertificationRepository;
import com.hrms.skills.repository.EmployeeSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillsService {

    private final EmployeeSkillRepository skillRepository;
    private final EmployeeCertificationRepository certRepository;

    public SkillsDto.SkillResponse addSkill(UUID employeeId, SkillsDto.AddSkillRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeSkill skill = EmployeeSkill.builder()
                .employeeId(employeeId)
                .skillName(req.getSkillName())
                .skillType(req.getSkillType())
                .proficiencyLevel(req.getProficiencyLevel())
                .yearsOfExperience(req.getYearsOfExperience())
                .build();
        skill.setCompanyId(companyId);
        return toSkillResponse(skillRepository.save(skill));
    }

    @Transactional(readOnly = true)
    public List<SkillsDto.SkillResponse> getSkills(UUID employeeId) {
        return skillRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toSkillResponse).toList();
    }

    public void removeSkill(UUID employeeId, UUID skillId) {
        EmployeeSkill skill = skillRepository.findById(skillId)
                .filter(s -> s.getEmployeeId().equals(employeeId) && s.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
        skill.setActive(false);
        skillRepository.save(skill);
    }

    public SkillsDto.CertificationResponse addCertification(UUID employeeId, SkillsDto.AddCertificationRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        EmployeeCertification cert = EmployeeCertification.builder()
                .employeeId(employeeId)
                .certificationName(req.getCertificationName())
                .issuingOrganization(req.getIssuingOrganization())
                .issueDate(req.getIssueDate())
                .expiryDate(req.getExpiryDate())
                .credentialId(req.getCredentialId())
                .credentialUrl(req.getCredentialUrl())
                .build();
        cert.setCompanyId(companyId);
        return toCertResponse(certRepository.save(cert));
    }

    @Transactional(readOnly = true)
    public List<SkillsDto.CertificationResponse> getCertifications(UUID employeeId) {
        return certRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toCertResponse).toList();
    }

    public void removeCertification(UUID employeeId, UUID certId) {
        EmployeeCertification cert = certRepository.findById(certId)
                .filter(c -> c.getEmployeeId().equals(employeeId) && c.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found: " + certId));
        cert.setActive(false);
        certRepository.save(cert);
    }

    private SkillsDto.SkillResponse toSkillResponse(EmployeeSkill s) {
        SkillsDto.SkillResponse res = new SkillsDto.SkillResponse();
        res.setId(s.getId());
        res.setSkillName(s.getSkillName());
        res.setSkillType(s.getSkillType());
        res.setProficiencyLevel(s.getProficiencyLevel());
        res.setYearsOfExperience(s.getYearsOfExperience());
        return res;
    }

    private SkillsDto.CertificationResponse toCertResponse(EmployeeCertification c) {
        SkillsDto.CertificationResponse res = new SkillsDto.CertificationResponse();
        res.setId(c.getId());
        res.setCertificationName(c.getCertificationName());
        res.setIssuingOrganization(c.getIssuingOrganization());
        res.setIssueDate(c.getIssueDate());
        res.setExpiryDate(c.getExpiryDate());
        res.setCredentialId(c.getCredentialId());
        res.setCredentialUrl(c.getCredentialUrl());
        return res;
    }
}
