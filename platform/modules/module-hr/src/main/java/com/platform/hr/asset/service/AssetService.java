package com.platform.hr.asset.service;

import com.platform.api.PagedResponse;
import com.platform.api.PlatformException;
import com.platform.audit.AuditLog;
import com.platform.audit.Auditable;
import com.platform.hr.asset.dto.AssetDto;
import com.platform.hr.asset.entity.Asset;
import com.platform.hr.asset.entity.Asset.AssetStatus;
import com.platform.hr.asset.entity.Asset.AssetType;
import com.platform.hr.asset.repository.AssetRepository;
import com.platform.hr.employee.repository.EmployeeRepository;
import com.platform.notification.Notification;
import com.platform.notification.NotificationRequest;
import com.platform.notification.NotificationService;
import com.platform.tenancy.TenantContext;
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    @Auditable(entity = "Asset", action = AuditLog.AuditAction.CREATE)
    public AssetDto.Response create(AssetDto.CreateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());

        int seq = assetRepository.findMaxAssetCodeSequence(companyId) + 1;
        String assetCode = String.format("AST%05d", seq);

        Asset asset = Asset.builder()
            .assetCode(assetCode)
            .name(request.getName())
            .assetType(request.getAssetType())
            .serialNumber(request.getSerialNumber())
            .model(request.getModel())
            .brand(request.getBrand())
            .purchaseDate(request.getPurchaseDate())
            .warrantyExpiry(request.getWarrantyExpiry())
            .status(AssetStatus.AVAILABLE)
            .notes(request.getNotes())
            .build();
        asset.setCompanyId(companyId);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public AssetDto.Response findById(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return toResponse(getActive(id, companyId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetDto.Response> search(String query, AssetStatus status,
                                                    AssetType assetType, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return PagedResponse.of(
            assetRepository.search(companyId, query, status, assetType, pageable).map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<AssetDto.Response> findByEmployee(UUID employeeId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return assetRepository.findByAssignedToEmployeeIdAndCompanyIdAndIsActiveTrue(employeeId, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Auditable(entity = "Asset", action = AuditLog.AuditAction.UPDATE)
    public AssetDto.Response assign(UUID assetId, AssetDto.AssignRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Asset asset = getActive(assetId, companyId);

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw PlatformException.badRequest("Asset is not available. Current status: " + asset.getStatus());
        }
        employeeRepository.findByIdAndCompanyIdAndIsActiveTrue(request.getEmployeeId(), companyId)
            .orElseThrow(() -> PlatformException.notFound("Employee"));

        asset.setAssignedToEmployeeId(request.getEmployeeId());
        asset.setAssignedAt(LocalDateTime.now());
        asset.setStatus(AssetStatus.ASSIGNED);
        if (request.getNotes() != null) asset.setNotes(request.getNotes());

        notificationService.send(NotificationRequest.builder()
            .companyId(companyId)
            .recipientId(request.getEmployeeId())
            .title("Asset Assigned")
            .message("Asset '" + asset.getName() + "' (" + asset.getAssetCode() + ") has been assigned to you.")
            .type(Notification.NotificationType.GENERAL)
            .referenceId(assetId.toString())
            .referenceType("ASSET")
            .build());

        return toResponse(assetRepository.save(asset));
    }

    @Auditable(entity = "Asset", action = AuditLog.AuditAction.UPDATE)
    public AssetDto.Response returnAsset(UUID assetId, AssetDto.ReturnRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Asset asset = getActive(assetId, companyId);

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw PlatformException.badRequest("Asset is not currently assigned");
        }

        asset.setReturnedAt(LocalDateTime.now());
        asset.setAssignedToEmployeeId(null);
        asset.setStatus(request.getNewStatus() != null ? request.getNewStatus() : AssetStatus.AVAILABLE);
        if (request.getNotes() != null) asset.setNotes(request.getNotes());

        return toResponse(assetRepository.save(asset));
    }

    @Auditable(entity = "Asset", action = AuditLog.AuditAction.UPDATE)
    public AssetDto.Response update(UUID id, AssetDto.UpdateRequest request) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Asset asset = getActive(id, companyId);
        if (request.getName() != null) asset.setName(request.getName());
        if (request.getSerialNumber() != null) asset.setSerialNumber(request.getSerialNumber());
        if (request.getModel() != null) asset.setModel(request.getModel());
        if (request.getBrand() != null) asset.setBrand(request.getBrand());
        if (request.getPurchaseDate() != null) asset.setPurchaseDate(request.getPurchaseDate());
        if (request.getWarrantyExpiry() != null) asset.setWarrantyExpiry(request.getWarrantyExpiry());
        if (request.getNotes() != null) asset.setNotes(request.getNotes());
        if (request.getStatus() != null) asset.setStatus(request.getStatus());
        return toResponse(assetRepository.save(asset));
    }

    @Auditable(entity = "Asset", action = AuditLog.AuditAction.DELETE)
    public void retire(UUID id) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        Asset asset = getActive(id, companyId);
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw PlatformException.badRequest("Cannot retire an assigned asset. Return it first.");
        }
        asset.setStatus(AssetStatus.RETIRED);
        asset.setIsActive(false);
        assetRepository.save(asset);
    }

    // ── Scheduler: daily at 9am — warranty expiry reminders ───
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendWarrantyReminders() {
        List<UUID> companyIds = assetRepository.findAll().stream()
            .map(a -> a.getCompanyId()).distinct().toList();

        for (UUID companyId : companyIds) {
            LocalDate today = LocalDate.now();
            List<Asset> expiring = assetRepository.findWarrantyExpiring(
                companyId, today, today.plusDays(30));
            for (Asset asset : expiring) {
                log.info("Warranty expiring for asset: {} on {}", asset.getAssetCode(), asset.getWarrantyExpiry());
                // Notify IT admin — in production, look up IT admin by role
            }
        }
    }

    private Asset getActive(UUID id, UUID companyId) {
        return assetRepository.findByIdAndCompanyIdAndIsActiveTrue(id, companyId)
            .orElseThrow(() -> PlatformException.notFound("Asset"));
    }

    private AssetDto.Response toResponse(Asset a) {
        AssetDto.Response r = new AssetDto.Response();
        r.setId(a.getId()); r.setAssetCode(a.getAssetCode()); r.setName(a.getName());
        r.setAssetType(a.getAssetType()); r.setSerialNumber(a.getSerialNumber());
        r.setModel(a.getModel()); r.setBrand(a.getBrand());
        r.setPurchaseDate(a.getPurchaseDate()); r.setWarrantyExpiry(a.getWarrantyExpiry());
        r.setStatus(a.getStatus()); r.setAssignedToEmployeeId(a.getAssignedToEmployeeId());
        r.setAssignedAt(a.getAssignedAt()); r.setReturnedAt(a.getReturnedAt()); r.setNotes(a.getNotes());
        r.setWarrantyExpiringSoon(a.getWarrantyExpiry() != null
            && !a.getWarrantyExpiry().isBefore(LocalDate.now())
            && a.getWarrantyExpiry().isBefore(LocalDate.now().plusDays(30)));
        if (a.getAssignedToEmployeeId() != null) {
            employeeRepository.findById(a.getAssignedToEmployeeId()).ifPresent(e ->
                r.setAssignedToEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }
        return r;
    }
}
