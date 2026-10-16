package com.pulsepass.service;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService,
                       AuthorizationService authorizationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, "Bearer", jwtService.accessTokenTtlSeconds());
    }

    /**
     * Registro publico: crea User + credencial BCrypt + ROLE_USER de forma atomica.
     * Nunca emite token automaticamente (flujo registro -> login).
     */
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        return userService.register(request);
    }

    public AuthenticatedUserResponse me() {
        SecurityPrincipal principal = authorizationService.requirePrincipal();
        return new AuthenticatedUserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRoles().stream().map(Enum::name).toList());
    }
}
