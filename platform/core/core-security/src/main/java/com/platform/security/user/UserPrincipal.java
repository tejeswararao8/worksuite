package com.platform.security.user;

import com.platform.tenancy.TenantContext;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the currently authenticated user.
 * Available anywhere via SecurityUtils.getCurrentUser().
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID companyId;
    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.companyId = user.getCompanyId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.active = user.getStatus() == User.UserStatus.ACTIVE && !user.isLocked();
        this.authorities = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(p -> new SimpleGrantedAuthority(p.getName()))
            .collect(Collectors.toSet());
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }

    public boolean hasPermission(String permission) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(permission));
    }

    // Static helper — get current user from SecurityContext
    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return UUID.fromString(auth.getPrincipal().toString());
    }

    public static UUID currentCompanyId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    public static Set<String> currentPermissions() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }
}
