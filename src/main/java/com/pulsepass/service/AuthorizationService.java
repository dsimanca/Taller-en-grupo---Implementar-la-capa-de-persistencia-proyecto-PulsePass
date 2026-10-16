package com.pulsepass.service;

import com.pulsepass.security.SecurityPrincipal;
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
                .map(principal -> principal.getRoles().contains(com.pulsepass.domain.enums.Role.ADMIN))
                .orElse(false);
    }

    public boolean isStaffOrAdmin() {
        return currentPrincipal()
                .map(principal -> principal.getRoles().stream()
                        .anyMatch(role -> role == com.pulsepass.domain.enums.Role.STAFF
                                || role == com.pulsepass.domain.enums.Role.ADMIN))
                .orElse(false);
    }

    /**
     * Permite continuar si el usuario autenticado es el propietario indicado por id o un ADMIN.
     */
    public void requireSelfOrAdmin(Long ownerUserId) {
        SecurityPrincipal principal = requirePrincipal();
        if (principal.getId().equals(ownerUserId)) {
            return;
        }
        if (isAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    /**
     * Permite continuar si el usuario autenticado es el propietario indicado por email (case-insensitive) o un ADMIN.
     */
    public void requireSelfByEmailOrAdmin(String ownerEmail) {
        SecurityPrincipal principal = requirePrincipal();
        if (principal.getEmail() != null && ownerEmail != null
                && principal.getEmail().equalsIgnoreCase(ownerEmail)) {
            return;
        }
        if (isAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    /**
     * La compra solo puede hacerse a nombre de la identidad autenticada.
     */
    public void requirePurchaseForSelf(String userEmail) {
        SecurityPrincipal principal = requirePrincipal();
        if (principal.getEmail() == null || userEmail == null
                || !principal.getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Solo puede operar sobre sus propios recursos");
        }
    }

    /**
     * Acceso a un ticket: propietario o STAFF/ADMIN en operacion autorizada.
     */
    public void requireTicketOwnerOrPrivileged(String ownerEmail) {
        SecurityPrincipal principal = requirePrincipal();
        if (principal.getEmail() != null && ownerEmail != null
                && principal.getEmail().equalsIgnoreCase(ownerEmail)) {
            return;
        }
        if (isStaffOrAdmin()) {
            return;
        }
        throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    /**
     * Cancelacion de ticket: propietario o ADMIN expresamente autorizado.
     */
    public void requireTicketOwnerOrAdmin(String ownerEmail) {
        requireSelfByEmailOrAdmin(ownerEmail);
    }
}
