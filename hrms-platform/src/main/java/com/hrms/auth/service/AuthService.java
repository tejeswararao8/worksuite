package com.hrms.auth.service;

import com.hrms.auth.dto.AuthDto;
import com.hrms.common.exception.BusinessException;
import com.hrms.security.jwt.JwtTokenProvider;
import com.hrms.security.jwt.TokenBlacklist;
import com.hrms.security.service.UserPrincipal;
import com.hrms.user.entity.User;
import com.hrms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "Invalid credentials"));

        if (user.isLocked()) {
            throw new BusinessException("ACCOUNT_LOCKED", "Account is locked. Try again later.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            resetFailedAttempts(user);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("User logged in: {}", request.getEmail());
            return buildTokenResponse(principal);
        } catch (Exception ex) {
            handleFailedLogin(user);
            throw new BusinessException("AUTH_FAILED", "Invalid credentials");
        }
    }

    public AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("INVALID_TOKEN", "Invalid or expired refresh token");
        }
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "User not found"));
        UserPrincipal principal = new UserPrincipal(user);
        return buildTokenResponse(principal);
    }

    public void logout(String accessToken, String refreshToken) {
        long ttl = expirationMs / 1000;
        if (accessToken  != null) tokenBlacklist.blacklist(accessToken, ttl);
        if (refreshToken != null) tokenBlacklist.blacklist(refreshToken, ttl * 7);
        log.info("Tokens blacklisted on logout");
    }

    public void changePassword(String email, AuthDto.ChangePasswordRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private AuthDto.TokenResponse buildTokenResponse(UserPrincipal principal) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                principal.getId(), principal.getCompanyId(), principal.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                principal.getId(), principal.getCompanyId(), principal.getUsername());
        String role = principal.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .findFirst().map(a -> a.getAuthority().substring(5)).orElse("");
        return new AuthDto.TokenResponse(accessToken, refreshToken, expirationMs / 1000,
                principal.getId(), principal.getCompanyId(), principal.getUsername(), role);
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked due to failed attempts: {}", user.getEmail());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
    }
}
