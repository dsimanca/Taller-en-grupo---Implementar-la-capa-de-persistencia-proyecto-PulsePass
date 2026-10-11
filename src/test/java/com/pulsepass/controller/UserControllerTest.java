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
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.service.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    private static final String USER_JSON = "{\"username\":\"andrea\",\"email\":\"andrea@email.com\",\"firstName\":\"Andrea\",\"lastName\":\"Perez\",\"phone\":\"3001234567\",\"city\":\"Bogota\",\"birthDate\":\"2000-01-01\"}";
    @Test void registersUser() throws Exception { when(userService.register(any())).thenReturn(user()); mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("andrea@email.com")); verify(userService).register(any(RegisterUserRequest.class)); }
    @Test void rejectsInvalidEmail() throws Exception { mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON.replace("andrea@email.com", "invalid"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.email").exists()); verify(userService, never()).register(any()); }
    @Test void returns409ForDuplicateUsername() throws Exception { when(userService.register(any())).thenThrow(new DuplicateResourceException("Username already exists")); mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(userService).register(any(RegisterUserRequest.class)); }
    @Test void findsUserByEmail() throws Exception { when(userService.findByEmail("andrea@email.com")).thenReturn(user()); mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com")).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea")); verify(userService).findByEmail("andrea@email.com"); }
    @Test void findsUserByUsername() throws Exception { when(userService.findByUsername("andrea")).thenReturn(user()); mockMvc.perform(get("/api/users/by-username").param("username", "andrea")).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("andrea@email.com")); verify(userService).findByUsername("andrea"); }
    private UserResponse user() { return new UserResponse(1L, "andrea", "andrea@email.com", true, "Andrea", "Perez", "3001234567", "Bogota", LocalDate.of(2000, 1, 1)); }
}
