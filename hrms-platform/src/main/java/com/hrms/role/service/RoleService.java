package com.hrms.role.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.role.dto.RoleDto;
import com.hrms.role.entity.Permission;
import com.hrms.role.entity.Role;
import com.hrms.role.repository.PermissionRepository;
import com.hrms.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleDto.Response create(RoleDto.CreateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (roleRepository.existsByNameAndCompanyId(req.getName(), companyId))
            throw new DuplicateResourceException("Role already exists: " + req.getName());

        Set<Permission> permissions = resolvePermissions(req.getPermissionIds());
        Role role = Role.builder()
                .name(req.getName())
                .description(req.getDescription())
                .permissions(permissions)
                .build();
        role.setCompanyId(companyId);
        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<RoleDto.Response> listAll() {
        return roleRepository.findByCompanyIdAndActiveTrue(SecurityUtils.getCurrentCompanyId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoleDto.Response getById(UUID id) {
        return toResponse(findRole(id));
    }

    public RoleDto.Response updatePermissions(UUID id, RoleDto.UpdatePermissionsRequest req) {
        Role role = findRole(id);
        role.setPermissions(resolvePermissions(req.getPermissionIds()));
        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<RoleDto.PermissionResponse> listPermissions() {
        return permissionRepository.findAllByActiveTrue().stream().map(this::toPermResponse).toList();
    }

    private Role findRole(UUID id) {
        return roleRepository.findById(id)
                .filter(r -> r.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private Set<Permission> resolvePermissions(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(permissionRepository.findAllById(ids));
    }

    private RoleDto.Response toResponse(Role r) {
        RoleDto.Response res = new RoleDto.Response();
        res.setId(r.getId());
        res.setName(r.getName());
        res.setDescription(r.getDescription());
        res.setPermissions(r.getPermissions().stream().map(this::toPermResponse).collect(Collectors.toSet()));
        return res;
    }

    private RoleDto.PermissionResponse toPermResponse(Permission p) {
        RoleDto.PermissionResponse res = new RoleDto.PermissionResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setDescription(p.getDescription());
        res.setModule(p.getModule());
        return res;
    }
}
