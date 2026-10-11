package com.pulsepass.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VenueController.class)
@Import(GlobalExceptionHandler.class)
class VenueControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private VenueService venueService;

    @Test void findsVenue() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(venue());
        mockMvc.perform(get("/api/venues/VEN-SMR-01")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON)).andExpect(jsonPath("$.code").value("VEN-SMR-01"));
        verify(venueService).findByCode("VEN-SMR-01");
    }
    @Test void returns404ForUnknownVenue() throws Exception {
        when(venueService.findByCode("MISSING")).thenThrow(new ResourceNotFoundException("Venue not found"));
        mockMvc.perform(get("/api/venues/MISSING")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        verify(venueService).findByCode("MISSING");
    }
    @Test void listsActiveVenues() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of(venue()));
        mockMvc.perform(get("/api/venues/active")).andExpect(status().isOk()).andExpect(jsonPath("$[0].active").value(true));
        verify(venueService).findActiveVenues();
    }
    private VenueResponse venue() { return new VenueResponse(1L, "VEN-SMR-01", "Marina", "Santa Marta", "Calle 1", 3, true); }
}
