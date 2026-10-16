package com.pulsepass.security;

import com.pulsepass.domain.enums.Role;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean active;
    private final List<Role> roles;

    public SecurityPrincipal(Long id,
                             String username,
                             String email,
                             String password,
                             boolean active,
                             List<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
