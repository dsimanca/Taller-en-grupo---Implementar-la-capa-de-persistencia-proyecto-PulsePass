package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.Role;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private AuthenticationEntryPoint authenticationEntryPoint;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService, authenticationEntryPoint);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private SecurityPrincipal principal(boolean active) {
        return new SecurityPrincipal(7L, "andrea", "andrea@email.com", "x", active, List.of(Role.USER));
    }

    private MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void continuesWithoutAuthenticationWhenThereIsNoBearerHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        run(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService, authenticationEntryPoint);
    }

    @Test
    void authenticatesAValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        when(jwtService.extractUserId("good-token")).thenReturn("7");
        when(userDetailsService.loadUserById(7L)).thenReturn(principal(true));

        run(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(SecurityPrincipal.class);
    }

    @Test
    void doesNotAuthenticateADisabledUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        when(jwtService.extractUserId("good-token")).thenReturn("7");
        when(userDetailsService.loadUserById(7L)).thenReturn(principal(false));

        run(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationEntryPoint).commence(eq(request), any(), any(BadCredentialsException.class));
    }

    @Test
    void clearsTheContextWhenTheTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer broken-token");
        when(jwtService.extractUserId("broken-token")).thenThrow(new JwtException("invalid"));

        run(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationEntryPoint).commence(eq(request), any(), any(BadCredentialsException.class));
    }

    @Test
    void doesNotAuthenticateWhenAuthorizationHeaderIsDuplicated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer other-token");

        run(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService);
        verify(authenticationEntryPoint).commence(eq(request), any(), any(BadCredentialsException.class));
    }
}
