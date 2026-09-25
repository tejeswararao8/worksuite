package com.hrms.user.service;

import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.common.util.SecurityUtils;
import com.hrms.role.entity.Role;
import com.hrms.role.repository.RoleRepository;
import com.hrms.user.dto.UserDto;
import com.hrms.user.entity.User;
import com.hrms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.Response create(UserDto.CreateRequest req) {
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        if (userRepository.existsByEmailAndCompanyId(req.getEmail(), companyId))
            throw new DuplicateResourceException("User already exists: " + req.getEmail());

        Role role = findRole(req.getRoleId(), companyId);
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .employeeId(req.getEmployeeId())
                .role(role)
                .mustChangePassword(true)
                .build();
        user.setCompanyId(companyId);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto.Response getById(UUID id) {
        return toResponse(findUser(id));
    }

    public UserDto.Response changeRole(UUID id, UserDto.ChangeRoleRequest req) {
        User user = findUser(id);
        user.setRole(findRole(req.getRoleId(), user.getCompanyId()));
        return toResponse(userRepository.save(user));
    }

    public void deactivate(UUID id) {
        User user = findUser(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activate(UUID id) {
        User user = findUser(id);
        user.setActive(true);
        userRepository.save(user);
    }

    public void resetPassword(UUID id, UserDto.ResetPasswordRequest req) {
        User user = findUser(id);
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(SecurityUtils.getCurrentCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Role findRole(UUID roleId, UUID companyId) {
        return roleRepository.findById(roleId)
                .filter(r -> r.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private UserDto.Response toResponse(User u) {
        UserDto.Response res = new UserDto.Response();
        res.setId(u.getId());
        res.setEmail(u.getEmail());
        res.setEmployeeId(u.getEmployeeId());
        res.setRoleName(u.getRole().getName());
        res.setActive(u.isActive());
        res.setLastLoginAt(u.getLastLoginAt());
        res.setMustChangePassword(u.isMustChangePassword());
        return res;
    }
}
