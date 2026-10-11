package com.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private EventService eventService;
    private static final String EVENT_JSON = "{\"eventCode\":\"CMF-2026\",\"name\":\"Caribbean Music Fest\",\"description\":\"Festival\",\"category\":\"MUSIC\",\"eventDate\":\"2027-01-15T20:00:00\",\"minimumAge\":18,\"venueCode\":\"VEN-SMR-01\"}";
    @Test void createsEvent() throws Exception { when(eventService.create(any())).thenReturn(event()); mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content(EVENT_JSON)).andExpect(status().isCreated()).andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON)).andExpect(jsonPath("$.eventCode").value("CMF-2026")); verify(eventService).create(any(CreateEventRequest.class)); }
    @Test void rejectsInvalidEvent() throws Exception { mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.eventCode").exists()); verify(eventService, never()).create(any()); }
    @Test void findsEvent() throws Exception { when(eventService.findByCode("CMF-2026")).thenReturn(event()); mockMvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Caribbean Music Fest")); verify(eventService).findByCode("CMF-2026"); }
    @Test void returns404ForUnknownEvent() throws Exception { when(eventService.findByCode("MISSING")).thenThrow(new ResourceNotFoundException("Event not found")); mockMvc.perform(get("/api/events/MISSING")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)); verify(eventService).findByCode("MISSING"); }
    @Test void listsPublishedEvents() throws Exception { when(eventService.findPublishedEvents()).thenReturn(List.of(summary())); mockMvc.perform(get("/api/events/published")).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("PUBLISHED")); verify(eventService).findPublishedEvents(); }
    @Test void publishesEvent() throws Exception { when(eventService.publish("CMF-2026")).thenReturn(event()); mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT")); verify(eventService).publish("CMF-2026"); }
    @Test void returns409WhenPublishingIsInvalid() throws Exception { when(eventService.publish("CMF-2026")).thenThrow(new BusinessRuleException("Invalid transition")); mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(eventService).publish("CMF-2026"); }
    @Test void addsArtist() throws Exception { when(eventService.addArtist("CMF-2026", 1L)).thenReturn(event()); mockMvc.perform(post("/api/events/CMF-2026/artists/1")).andExpect(status().isOk()).andExpect(jsonPath("$.eventCode").value("CMF-2026")); verify(eventService).addArtist("CMF-2026", 1L); }
    @Test void findsEventsByArtist() throws Exception { when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(summary())); mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat")).andExpect(status().isOk()).andExpect(jsonPath("$[0].eventCode").value("CMF-2026")); verify(eventService).findByArtist("Solar Beat"); }
    private EventResponse event() { return new EventResponse(1L, "CMF-2026", "Caribbean Music Fest", "Festival", EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.of(2027, 1, 15, 20, 0), 18, "VEN-SMR-01", "Marina", List.of()); }
    private EventSummaryResponse summary() { return new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest", EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2027, 1, 15, 20, 0), "Marina"); }
}
