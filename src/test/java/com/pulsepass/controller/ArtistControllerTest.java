package com.pulsepass.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArtistController.class)
@Import(GlobalExceptionHandler.class)
class ArtistControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ArtistService artistService;
    @Test void findsArtistById() throws Exception { when(artistService.findById(1L)).thenReturn(artist()); mockMvc.perform(get("/api/artists/1")).andExpect(status().isOk()).andExpect(jsonPath("$.stageName").value("Solar Beat")); verify(artistService).findById(1L); }
    @Test void returns404ForUnknownArtist() throws Exception { when(artistService.findById(99L)).thenThrow(new ResourceNotFoundException("Artist not found")); mockMvc.perform(get("/api/artists/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)); verify(artistService).findById(99L); }
    @Test void findsArtistByStageName() throws Exception { when(artistService.findByStageName("Solar Beat")).thenReturn(artist()); mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)); verify(artistService).findByStageName("Solar Beat"); }
    @Test void listsActiveArtists() throws Exception { when(artistService.findActiveArtists()).thenReturn(List.of(artist())); mockMvc.perform(get("/api/artists/active")).andExpect(status().isOk()).andExpect(jsonPath("$[0].active").value(true)); verify(artistService).findActiveArtists(); }
    private ArtistResponse artist() { return new ArtistResponse(1L, "Solar Beat", "Colombia", "Pop", true); }
}
