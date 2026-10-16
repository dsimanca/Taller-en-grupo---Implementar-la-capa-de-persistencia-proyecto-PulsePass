package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.Role;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.service.AuthService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;

/**
 * Flujo de seguridad extremo a extremo sobre PostgreSQL real (Testcontainers):
 * registro atomico -> credencial BCrypt + ROLE_USER -> login -> JWT verificable.
 * Cubre la integracion FR-SEC-AUTH-001 y el escenario T-SEC-005 del PRD.
 */
class SecurityFlowIT extends BaseIntegrationTest {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    @Autowired private AuthService authService;
    @Autowired private UserCredentialRepository userCredentialRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private JwtService jwtService;
    @Autowired private CustomUserDetailsService userDetailsService;

    private RegisterUserRequest newUser(String username, String email) {
        return new RegisterUserRequest(username, email, PASSWORD,
                "Andrea", "Perez", "3001234567", "Bogota", LocalDate.of(1995, 5, 20));
    }

    @Test
    void registrationStoresABcryptCredentialAndTheUserRole() {
        UserResponse user = authService.register(newUser("andrea_it", "andrea_it@email.com"));

        String hash = userCredentialRepository.findByUserId(user.id()).orElseThrow().getPasswordHash();
        assertThat(hash).isNotEqualTo(PASSWORD);
        assertThat(hash).startsWith("$2");
        assertThat(userRoleRepository.findRolesByUserId(user.id())).containsExactly(Role.USER);
    }

    @Test
    void loginWithUsernameOrEmailIssuesATokenForTheUser() {
        UserResponse user = authService.register(newUser("carlos_it", "carlos_it@email.com"));

        AuthResponse byUsername = authService.login(new LoginRequest("carlos_it", PASSWORD));
        AuthResponse byEmail = authService.login(new LoginRequest("carlos_it@email.com", PASSWORD));

        assertThat(byUsername.tokenType()).isEqualTo("Bearer");
        assertThat(jwtService.extractUserId(byUsername.accessToken())).isEqualTo(user.id().toString());
        assertThat(jwtService.extractUserId(byEmail.accessToken())).isEqualTo(user.id().toString());
    }

    @Test
    void loginWithAWrongPasswordFails() {
        authService.register(newUser("laura_it", "laura_it@email.com"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("laura_it", "wrong-password")))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void anIssuedTokenResolvesToAnActivePrincipalWithItsRoles() {
        UserResponse user = authService.register(newUser("miguel_it", "miguel_it@email.com"));

        SecurityPrincipal principal = userDetailsService.loadUserById(user.id());

        assertThat(principal.getUsername()).isEqualTo("miguel_it");
        assertThat(principal.getRoles()).containsExactly(Role.USER);
        assertThat(principal.isEnabled()).isTrue();
    }
}
