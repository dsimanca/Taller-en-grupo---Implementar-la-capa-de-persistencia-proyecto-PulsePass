package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserProfile;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.Impl.TicketServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private TicketMapper ticketMapper;
    private TicketService service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(ticketRepository, userRepository, eventRepository, ticketMapper);
    }

    @Test
    void purchasesAPaidTicketForAnEligibleUser() {
        User user = user(true, LocalDate.now().minusYears(25));
        Event event = event(EventStatus.PUBLISHED, 18, 3);
        preparePurchase(user, event, 0);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.purchase(request());

        ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(saved.getValue().getPrice()).isPositive();
    }

    @Test
    void rejectsAnUnknownUser() {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsAnInactiveUser() {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user(false, LocalDate.now().minusYears(25))));

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).findByEventCode(any());
    }

    @Test
    void rejectsADraftEvent() {
        preparePurchase(user(true, LocalDate.now().minusYears(25)), event(EventStatus.DRAFT, 0, 3), 0);

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsACancelledEvent() {
        preparePurchase(user(true, LocalDate.now().minusYears(25)), event(EventStatus.CANCELLED, 0, 3), 0);

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsAnUnderageUser() {
        preparePurchase(user(true, LocalDate.now().minusYears(17)), event(EventStatus.PUBLISHED, 18, 3), 0);

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsAnAgeRestrictedPurchaseWithoutBirthDate() {
        preparePurchase(user(true, null), event(EventStatus.PUBLISHED, 18, 3), 0);

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsAnEventAtCapacity() {
        preparePurchase(user(true, LocalDate.now().minusYears(25)), event(EventStatus.PUBLISHED, 0, 3), 3);

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void marksTheEventSoldOutAfterTheLastTicket() {
        User user = user(true, LocalDate.now().minusYears(25));
        Event event = event(EventStatus.PUBLISHED, 0, 3);
        preparePurchase(user, event, 2);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(event)).thenReturn(event);

        service.purchase(request());

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(eventRepository).save(event);
    }

    @Test
    void cancelsAPaidTicketBeforeTheEvent() {
        Ticket ticket = ticket(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        service.cancel("TCK-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }

    @Test
    void rejectsCancellationOfAUsedTicket() {
        Ticket ticket = ticket(TicketStatus.USED);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TCK-1")).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(eq(ticket));
    }

    @Test
    void marksAPaidTicketAsUsed() {
        Ticket ticket = ticket(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        service.markAsUsed("TCK-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
    }

    @Test
    void rejectsUsingACancelledTicket() {
        Ticket ticket = ticket(TicketStatus.CANCELLED);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TCK-1")).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(eq(ticket));
    }

    private void preparePurchase(User user, Event event, long paidTickets) {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        lenient().when(ticketRepository.countByEventEventCodeAndStatus("EV-1", TicketStatus.PAID)).thenReturn(paidTickets);
    }

    private PurchaseTicketRequest request() {
        return new PurchaseTicketRequest("andrea@email.com", "EV-1", TicketType.GENERAL);
    }

    private User user(boolean active, LocalDate birthDate) {
        User user = new User();
        user.setEmail("andrea@email.com");
        user.setActive(active);
        UserProfile profile = new UserProfile();
        profile.setBirthDate(birthDate);
        user.setProfile(profile);
        return user;
    }

    private Event event(EventStatus status, int minimumAge, int capacity) {
        Venue venue = new Venue();
        venue.setCapacity(capacity);
        Event event = new Event();
        event.setEventCode("EV-1");
        event.setStatus(status);
        event.setMinimumAge(minimumAge);
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setVenue(venue);
        return event;
    }

    private Ticket ticket(TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-1");
        ticket.setStatus(status);
        ticket.setEvent(event(EventStatus.PUBLISHED, 0, 3));
        return ticket;
    }
}
