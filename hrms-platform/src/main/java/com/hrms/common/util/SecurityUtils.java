package com.hrms.common.util;

import com.hrms.security.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    public static UUID getCurrentCompanyId() {
        return getCurrentUser().map(UserPrincipal::getCompanyId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    public static String getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getUsername)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }
}
