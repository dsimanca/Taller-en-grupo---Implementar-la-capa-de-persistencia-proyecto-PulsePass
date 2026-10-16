package com.pulsepass.service;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.security.JwtProperties;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String REQUEST_ID_KEY = "requestId";
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       JwtProperties jwtProperties, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        return userService.register(request);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            if (request.password() == null
                    || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials");
            }
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password()));
        } catch (AuthenticationException exception) {
            logger.warn("Authentication failed requestId={}", MDC.get(REQUEST_ID_KEY));
            throw exception;
        }
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        logger.info("Authentication succeeded requestId={}", MDC.get(REQUEST_ID_KEY));
        return new AuthResponse(jwtService.generateToken(principal), "Bearer",
                jwtProperties.expiration().toSeconds());
    }

    public AuthenticatedUserResponse currentUser(SecurityPrincipal principal) {
        var roles = principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().substring("ROLE_".length()))
                .collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUserResponse(principal.userId(), principal.username(), principal.email(), roles);
    }
}
