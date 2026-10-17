package com.pulsepass.service;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.security.SecurityPrincipal;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public Optional<SecurityPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof SecurityPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public SecurityPrincipal requirePrincipal() {
        return currentPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Authentication is required"));
    }

    public boolean isAdmin() {
        return currentPrincipal()
                .map(principal -> principal.getRoles().contains(Role.ADMIN))
                .orElse(false);
    }

    public boolean isStaffOrAdmin() {
        return currentPrincipal()
                .map(principal -> principal.getRoles().stream()
                        .anyMatch(role -> role == Role.STAFF || role == Role.ADMIN))
                .orElse(false);
    }

    public void requireStaffOrAdmin() {
        if (!isStaffOrAdmin()) {
            throw new AccessDeniedException("No tiene permisos para realizar esta operacion");
        }
    }

    /**
     * Permite continuar si el usuario autenticado es el propietario indicado por id o un ADMIN.
     */
    public void requireSelfOrAdmin(Long ownerUserId) {
        SecurityPrincipal principal = requirePrincipal();
        if (ownerUserId != null && principal.getRoles().contains(Role.USER)
                && Objects.equals(principal.getId(), ownerUserId)) {
            return;
        }
        if (isAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    public void requirePurchaseForSelf(Long ownerUserId) {
        SecurityPrincipal principal = requirePrincipal();
        if (!principal.getRoles().contains(Role.USER)
                || ownerUserId == null
                || !Objects.equals(principal.getId(), ownerUserId)) {
            throw new AccessDeniedException("Solo puede operar sobre sus propios recursos");
        }
    }

    public void requireTicketOwnerOrPrivileged(Long ownerUserId) {
        SecurityPrincipal principal = requirePrincipal();
        if (ownerUserId != null && Objects.equals(principal.getId(), ownerUserId)) {
            return;
        }
        if (isStaffOrAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    public void requireTicketOwnerOrAdmin(Long ownerUserId) {
        SecurityPrincipal principal = requirePrincipal();
        if (ownerUserId != null && principal.getRoles().contains(Role.USER)
                && Objects.equals(principal.getId(), ownerUserId)) {
            return;
        }
        if (isAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }
}
