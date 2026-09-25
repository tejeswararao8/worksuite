package com.hrms.security.service;

import com.hrms.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID companyId;
    private final String username;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.companyId = user.getCompanyId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.active = user.isActive();
        this.authorities = buildAuthorities(user);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        List<SimpleGrantedAuthority> auths = new java.util.ArrayList<>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        user.getRole().getPermissions().forEach(p ->
                auths.add(new SimpleGrantedAuthority(p.getName())));
        return auths;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
