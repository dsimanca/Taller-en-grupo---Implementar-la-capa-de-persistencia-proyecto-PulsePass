package com.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class})
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private com.pulsepass.security.JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void logsInWithoutAnExistingSession() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("signed.jwt.token", "Bearer", 900));
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"identifier\":\"andrea\",\"password\":\"ValidPassword-2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void rejectsMissingCredentialsBeforeCallingAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"identifier\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void requiresAuthenticationForCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }
}
