package com.pulsepass.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record SecurityPrincipal(Long userId, String username, String email, String password,
                                boolean enabled, Collection<? extends GrantedAuthority> authorities) implements UserDetails {
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return enabled; }
}
