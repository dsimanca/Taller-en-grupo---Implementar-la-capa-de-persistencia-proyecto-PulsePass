package com.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.EnableSecuritySlice;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.AuthService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@EnableSecuritySlice
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @Test void logsInAndReturnsAToken() throws Exception { when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", "Bearer", 900)); mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content("{\"identifier\":\"andrea\",\"password\":\"UnaFraseSeguraLarga2026!\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("jwt-token")).andExpect(jsonPath("$.tokenType").value("Bearer")); }
    @Test void rejectsAnIncompleteLoginRequest() throws Exception { mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.identifier").exists()); }
    @Test void returns401ForBadCredentials() throws Exception { when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials")); mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content("{\"identifier\":\"andrea\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid credentials")); }
    @Test @WithMockUser(username = "andrea") void returnsTheAuthenticatedUser() throws Exception { when(authService.me()).thenReturn(new AuthenticatedUserResponse(1L, "andrea", "andrea@email.com", List.of("USER"))); mockMvc.perform(get("/api/auth/me")).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea")).andExpect(jsonPath("$.roles[0]").value("USER")); }
    @Test void rejectsAnonymousMe() throws Exception { mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()); }
}
