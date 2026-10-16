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

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.EnableSecuritySlice;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@EnableSecuritySlice
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    private static final String USER_JSON = "{\"username\":\"andrea\",\"email\":\"andrea@email.com\",\"password\":\"UnaFraseSeguraLarga2026!\",\"firstName\":\"Andrea\",\"lastName\":\"Perez\",\"phone\":\"3001234567\",\"city\":\"Bogota\",\"birthDate\":\"2000-01-01\"}";
    @Test void registersUser() throws Exception { when(authService.register(any())).thenReturn(user()); mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("andrea@email.com")); verify(authService).register(any(RegisterUserRequest.class)); }
    @Test void rejectsInvalidEmail() throws Exception { mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON.replace("andrea@email.com", "invalid"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.email").exists()); verify(authService, never()).register(any()); }
    @Test void rejectsShortPassword() throws Exception { mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON.replace("UnaFraseSeguraLarga2026!", "corta"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.password").exists()); verify(authService, never()).register(any()); }
    @Test void returns409ForDuplicateUsername() throws Exception { when(authService.register(any())).thenThrow(new DuplicateResourceException("Username already exists")); mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(authService).register(any(RegisterUserRequest.class)); }
    @Test @WithMockUser(username = "andrea") void findsUserByEmail() throws Exception { when(userService.findByEmail("andrea@email.com")).thenReturn(user()); mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com")).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea")); verify(userService).findByEmail("andrea@email.com"); }
    @Test @WithMockUser(username = "andrea") void findsUserByUsername() throws Exception { when(userService.findByUsername("andrea")).thenReturn(user()); mockMvc.perform(get("/api/users/by-username").param("username", "andrea")).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("andrea@email.com")); verify(userService).findByUsername("andrea"); }
    @Test void rejectsAnonymousUserLookup() throws Exception { mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com")).andExpect(status().isUnauthorized()); verify(userService, never()).findByEmail(any()); }
    private UserResponse user() { return new UserResponse(1L, "andrea", "andrea@email.com", true, "Andrea", "Perez", "3001234567", "Bogota", LocalDate.of(2000, 1, 1)); }
}
