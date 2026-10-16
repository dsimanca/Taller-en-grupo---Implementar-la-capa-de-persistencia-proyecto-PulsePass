package com.pulsepass.security;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.controller.ArtistController;
import com.pulsepass.controller.AuthController;
import com.pulsepass.controller.EventController;
import com.pulsepass.controller.TicketController;
import com.pulsepass.controller.UserController;
import com.pulsepass.controller.VenueController;
import com.pulsepass.service.ArtistService;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import com.pulsepass.service.UserService;
import com.pulsepass.service.VenueService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica la matriz de autorizacion de la Fase 4 con la cadena real de Spring Security.
 * Cada endpoint se prueba en sus dos bordes relevantes: identidad anonima y rol insuficiente.
 */
@WebMvcTest(controllers = {
        AuthController.class,
        UserController.class,
        EventController.class,
        TicketController.class,
        ArtistController.class,
        VenueController.class
})
@EnableSecuritySlice
class SecurityAuthorizationMatrixTest {

    private static final String EVENT_JSON =
            "{\"eventCode\":\"CMF-2026\",\"name\":\"Festival\",\"category\":\"MUSIC\","
                    + "\"eventDate\":\"2027-01-15T20:00:00\",\"minimumAge\":18,\"venueCode\":\"VEN-1\"}";
    private static final String TICKET_JSON =
            "{\"userEmail\":\"andrea@email.com\",\"eventCode\":\"CMF-2026\",\"type\":\"GENERAL\"}";
    private static final String LOGIN_JSON =
            "{\"identifier\":\"andrea\",\"password\":\"UnaFraseSeguraLarga2026!\"}";
    private static final String USER_JSON =
            "{\"username\":\"andrea\",\"email\":\"andrea@email.com\","
                    + "\"password\":\"UnaFraseSeguraLarga2026!\",\"firstName\":\"Andrea\","
                    + "\"lastName\":\"Perez\",\"phone\":\"300\",\"city\":\"Bogota\","
                    + "\"birthDate\":\"2000-01-01\"}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private EventService eventService;
    @MockitoBean private TicketService ticketService;
    @MockitoBean private ArtistService artistService;
    @MockitoBean private VenueService venueService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    // --- Endpoints publicos ---

    @Test
    void loginIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void registrationIsPublic() throws Exception {
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(USER_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void publishedEventsArePublic() throws Exception {
        mockMvc.perform(get("/api/events/published")).andExpect(status().isOk());
    }

    @Test
    void eventByCodeIsPublic() throws Exception {
        mockMvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk());
    }

    @Test
    void eventsByArtistArePublic() throws Exception {
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk());
    }

    @Test
    void artistsArePublic() throws Exception {
        mockMvc.perform(get("/api/artists/1")).andExpect(status().isOk());
    }

    @Test
    void venuesArePublic() throws Exception {
        mockMvc.perform(get("/api/venues/VEN-1")).andExpect(status().isOk());
    }

    // --- Administracion (ADMIN) ---

    @Test
    void anonymousCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content(EVENT_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content(EVENT_JSON))
                .andExpect(status().isForbidden());
        verify(eventService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content(EVENT_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotPublishEvent() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanPublishEvent() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddArtist() throws Exception {
        mockMvc.perform(post("/api/events/CMF-2026/artists/1")).andExpect(status().isOk());
    }

    // --- Operacion (STAFF/ADMIN) ---

    @Test
    void anonymousCannotListPaidTickets() throws Exception {
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotListPaidTickets() throws Exception {
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanListPaidTickets() throws Exception {
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotMarkTicketAsUsed() throws Exception {
        mockMvc.perform(patch("/api/tickets/TCK-1/use")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanMarkTicketAsUsed() throws Exception {
        mockMvc.perform(patch("/api/tickets/TCK-1/use")).andExpect(status().isOk());
    }

    // --- Autenticados ---

    @Test
    void anonymousCannotAccessMe() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotPurchase() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(TICKET_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUserCanPurchase() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(TICKET_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCannotFindTicket() throws Exception {
        mockMvc.perform(get("/api/tickets/TCK-1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUserCanFindTicket() throws Exception {
        mockMvc.perform(get("/api/tickets/TCK-1")).andExpect(status().isOk());
    }

    @Test
    void anonymousCannotFindUserByEmail() throws Exception {
        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com"))
                .andExpect(status().isUnauthorized());
    }

    // --- Tokens invalidos (T-SEC-007/008/019) ---

    @Test
    void invalidBearerTokenOnProtectedRouteReturns401WithErrorResponse() throws Exception {
        when(jwtService.extractUserId(anyString())).thenThrow(new JwtException("invalid"));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer broken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void tamperedTokenDoesNotReachTheService() throws Exception {
        when(jwtService.extractUserId(anyString())).thenThrow(new JwtException("bad signature"));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer tampered")
                        .contentType(APPLICATION_JSON)
                        .content(EVENT_JSON))
                .andExpect(status().isUnauthorized());
        verify(eventService, never()).create(any());
    }
}
