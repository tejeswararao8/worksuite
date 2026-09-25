package com.hrms.asset.service;

import com.hrms.asset.dto.AssetDto;
import com.hrms.asset.entity.Asset;
import com.hrms.asset.entity.Asset.AssetStatus;
import com.hrms.asset.mapper.AssetMapper;
import com.hrms.asset.repository.AssetRepository;
import com.hrms.common.dto.PagedResponse;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public AssetDto.Response create(AssetDto.Request request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Asset asset = assetMapper.toEntity(request);
        asset.setCompanyId(companyId);
        asset.setAssetCode(generateAssetCode(companyId));
        asset.setStatus(AssetStatus.AVAILABLE);
        return assetMapper.toResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public AssetDto.Response findById(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return assetRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .map(assetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetDto.Response> findAll(Pageable pageable) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(assetRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(assetMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<AssetDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return assetRepository.findByAssignedToEmployeeIdAndCompanyIdAndActiveTrue(employeeId, companyId)
                .stream().map(assetMapper::toResponse).collect(Collectors.toList());
    }

    public AssetDto.Response assign(UUID id, AssetDto.AssignRequest request) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Asset asset = assetRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BusinessException("ASSET_NOT_AVAILABLE", "Asset is not available for assignment");
        }
        asset.setAssignedToEmployeeId(request.getEmployeeId());
        asset.setAssignedAt(LocalDateTime.now());
        asset.setStatus(AssetStatus.ASSIGNED);
        return assetMapper.toResponse(assetRepository.save(asset));
    }

    public AssetDto.Response returnAsset(UUID id) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        Asset asset = assetRepository.findByIdAndCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        asset.setAssignedToEmployeeId(null);
        asset.setReturnedAt(LocalDateTime.now());
        asset.setStatus(AssetStatus.AVAILABLE);
        return assetMapper.toResponse(assetRepository.save(asset));
    }

    private String generateAssetCode(UUID companyId) {
        int seq = assetRepository.findMaxAssetCodeSequence(companyId) + 1;
        return String.format("AST%05d", seq);
    }
}
