package com.pulsepass.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class AuthorizationService {
    public SecurityPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw new AccessDeniedException("An authenticated PulsePass user is required");
        }
        return principal;
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public boolean isStaffOrAdmin() {
        return hasRole("STAFF") || hasRole("ADMIN");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public void requireAnyRole(String... roles) {
        if (Arrays.stream(roles).noneMatch(this::hasRole)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    public void requireSelfOrAdmin(Long userId) {
        if (!isAdmin() && !currentPrincipal().userId().equals(userId)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    public void requireOwnEmailOrAdmin(String email) {
        if (!isAdmin() && !currentPrincipal().email().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    public void requireOwnUsernameOrAdmin(String username) {
        if (!isAdmin() && !currentPrincipal().username().equals(username)) {
            throw new AccessDeniedException("Access is denied");
        }
    }
}
