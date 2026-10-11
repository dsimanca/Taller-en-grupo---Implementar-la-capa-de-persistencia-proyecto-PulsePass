package com.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.TicketService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private TicketService ticketService;
    private static final String TICKET_JSON = "{\"userEmail\":\"andrea@email.com\",\"eventCode\":\"CMF-2026\",\"type\":\"GENERAL\"}";
    @Test void purchasesTicket() throws Exception { when(ticketService.purchase(any())).thenReturn(ticket()); mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(TICKET_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.ticketCode").value("TCK-001")); verify(ticketService).purchase(any(PurchaseTicketRequest.class)); }
    @Test void rejectsInvalidPurchase() throws Exception { mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.userEmail").exists()); verify(ticketService, never()).purchase(any()); }
    @Test void returns404WhenUserDoesNotExist() throws Exception { when(ticketService.purchase(any())).thenThrow(new ResourceNotFoundException("User not found")); mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(TICKET_JSON)).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)); verify(ticketService).purchase(any(PurchaseTicketRequest.class)); }
    @Test void returns409WhenPurchaseBreaksRule() throws Exception { when(ticketService.purchase(any())).thenThrow(new BusinessRuleException("User is underage")); mockMvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(TICKET_JSON)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(ticketService).purchase(any(PurchaseTicketRequest.class)); }
    @Test void findsTicket() throws Exception { when(ticketService.findByCode("TCK-001")).thenReturn(ticket()); mockMvc.perform(get("/api/tickets/TCK-001")).andExpect(status().isOk()).andExpect(jsonPath("$.ticketCode").value("TCK-001")); verify(ticketService).findByCode("TCK-001"); }
    @Test void findsTicketsByUser() throws Exception { when(ticketService.findByUserEmail("andrea@email.com")).thenReturn(List.of(ticket())); mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@email.com")).andExpect(status().isOk()).andExpect(jsonPath("$[0].userEmail").value("andrea@email.com")); verify(ticketService).findByUserEmail("andrea@email.com"); }
    @Test void findsPaidTicketsByEvent() throws Exception { when(ticketService.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket())); mockMvc.perform(get("/api/events/CMF-2026/tickets/paid")).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("PAID")); verify(ticketService).findPaidTicketsByEvent("CMF-2026"); }
    @Test void cancelsTicket() throws Exception { when(ticketService.cancel("TCK-001")).thenReturn(ticket()); mockMvc.perform(patch("/api/tickets/TCK-001/cancel")).andExpect(status().isOk()).andExpect(jsonPath("$.ticketCode").value("TCK-001")); verify(ticketService).cancel("TCK-001"); }
    @Test void rejectsInvalidCancellation() throws Exception { when(ticketService.cancel("TCK-001")).thenThrow(new BusinessRuleException("Ticket already used")); mockMvc.perform(patch("/api/tickets/TCK-001/cancel")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(ticketService).cancel("TCK-001"); }
    @Test void marksTicketAsUsed() throws Exception { when(ticketService.markAsUsed("TCK-001")).thenReturn(ticket()); mockMvc.perform(patch("/api/tickets/TCK-001/use")).andExpect(status().isOk()).andExpect(jsonPath("$.ticketCode").value("TCK-001")); verify(ticketService).markAsUsed("TCK-001"); }
    @Test void rejectsInvalidUse() throws Exception { when(ticketService.markAsUsed("TCK-001")).thenThrow(new BusinessRuleException("Ticket cancelled")); mockMvc.perform(patch("/api/tickets/TCK-001/use")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)); verify(ticketService).markAsUsed("TCK-001"); }
    private TicketResponse ticket() { return new TicketResponse(1L, "TCK-001", TicketType.GENERAL, new BigDecimal("150000"), TicketStatus.PAID, LocalDateTime.of(2026, 1, 1, 10, 0), "andrea@email.com", "CMF-2026", "Caribbean Music Fest"); }
}
