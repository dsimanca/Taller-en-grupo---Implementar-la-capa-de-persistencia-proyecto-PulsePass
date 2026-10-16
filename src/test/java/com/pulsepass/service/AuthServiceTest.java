package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserService userService;
    @Mock private AuthorizationService authorizationService;

    private AuthService service() {
        return new AuthService(authenticationManager, jwtService, userService, authorizationService);
    }

    private SecurityPrincipal principal() {
        return new SecurityPrincipal(7L, "andrea", "andrea@email.com", "$2a$hash", true,
                List.of(Role.USER, Role.STAFF));
    }

    @Test
    void logsInAndIssuesAToken() {
        SecurityPrincipal principal = principal();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);

        AuthResponse response = service().login(new LoginRequest("andrea", "UnaFraseSeguraLarga2026!"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void propagatesBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> service().login(new LoginRequest("andrea", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void delegatesRegistrationToTheUserService() {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea", "andrea@email.com", "UnaFraseSeguraLarga2026!",
                "Andrea", "Perez", "300", "Bogota", LocalDate.of(2000, 1, 1));
        UserResponse expected = new UserResponse(7L, "andrea", "andrea@email.com", true,
                "Andrea", "Perez", "300", "Bogota", LocalDate.of(2000, 1, 1));
        when(userService.register(request)).thenReturn(expected);

        assertThat(service().register(request)).isSameAs(expected);
        verify(userService).register(request);
    }

    @Test
    void describesTheAuthenticatedUser() {
        SecurityPrincipal principal = principal();
        when(authorizationService.requirePrincipal()).thenReturn(principal);

        AuthenticatedUserResponse me = service().me();

        assertThat(me.id()).isEqualTo(7L);
        assertThat(me.username()).isEqualTo("andrea");
        assertThat(me.roles()).containsExactly("USER", "STAFF");
    }
}
