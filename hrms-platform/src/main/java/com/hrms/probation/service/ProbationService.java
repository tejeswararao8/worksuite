package com.hrms.probation.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.probation.dto.ProbationDto;
import com.hrms.probation.entity.Probation;
import com.hrms.probation.entity.Probation.ProbationStatus;
import com.hrms.probation.repository.ProbationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProbationService {

    private final ProbationRepository probationRepository;

    public ProbationDto.Response initiate(ProbationDto.InitiateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        probationRepository.findByEmployeeIdAndCompanyIdAndStatus(req.getEmployeeId(), companyId, ProbationStatus.IN_PROGRESS)
                .ifPresent(p -> { throw new BusinessException("PROBATION_ACTIVE", "Employee already has an active probation"); });

        Probation probation = Probation.builder()
                .employeeId(req.getEmployeeId())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(ProbationStatus.IN_PROGRESS)
                .remarks(req.getRemarks())
                .build();
        probation.setCompanyId(companyId);
        return toResponse(probationRepository.save(probation));
    }

    @Transactional(readOnly = true)
    public List<ProbationDto.Response> getByEmployee(UUID employeeId) {
        return probationRepository.findByEmployeeIdAndCompanyId(employeeId, SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toResponse).toList();
    }

    public ProbationDto.Response confirm(UUID id) {
        return updateStatus(id, ProbationStatus.CONFIRMED);
    }

    public ProbationDto.Response extend(UUID id, ProbationDto.ExtendRequest req) {
        Probation p = findActive(id);
        p.setExtendedEndDate(req.getNewEndDate());
        p.setRemarks(req.getRemarks());
        p.setStatus(ProbationStatus.EXTENDED);
        return toResponse(probationRepository.save(p));
    }

    public ProbationDto.Response reject(UUID id, ProbationDto.ActionRequest req) {
        Probation p = findActive(id);
        p.setRemarks(req.getRemarks());
        p.setStatus(ProbationStatus.REJECTED);
        p.setApprovedBy(SecurityUtils.getCurrentUserId());
        p.setApprovedAt(LocalDateTime.now());
        return toResponse(probationRepository.save(p));
    }

    private ProbationDto.Response updateStatus(UUID id, ProbationStatus status) {
        Probation p = findActive(id);
        p.setStatus(status);
        p.setApprovedBy(SecurityUtils.getCurrentUserId());
        p.setApprovedAt(LocalDateTime.now());
        return toResponse(probationRepository.save(p));
    }

    private Probation findActive(UUID id) {
        Probation p = probationRepository.findById(id)
                .filter(pr -> pr.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Probation not found: " + id));
        if (p.getStatus() != ProbationStatus.IN_PROGRESS && p.getStatus() != ProbationStatus.EXTENDED)
            throw new BusinessException("INVALID_STATUS", "Probation is not in an actionable state");
        return p;
    }

    private ProbationDto.Response toResponse(Probation p) {
        ProbationDto.Response res = new ProbationDto.Response();
        res.setId(p.getId());
        res.setEmployeeId(p.getEmployeeId());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());
        res.setExtendedEndDate(p.getExtendedEndDate());
        res.setStatus(p.getStatus());
        res.setRemarks(p.getRemarks());
        res.setApprovedBy(p.getApprovedBy());
        res.setApprovedAt(p.getApprovedAt());
        return res;
    }
}
