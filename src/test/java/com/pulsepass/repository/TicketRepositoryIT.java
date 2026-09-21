package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class TicketRepositoryIT extends BaseIntegrationTest {
    @Autowired private TicketRepository tickets;

    @Test
    void shouldRejectDuplicateTicketCode() {
        User user = saveUser("miguel", "m@x.com");
        Event event = saveEvent("EV-T1", saveVenue("V-T1"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(3));

        Ticket first = buildTicket("TCK-0001", TicketType.VIP, TicketStatus.PAID,
                new BigDecimal("250000.00"), user, event);
        tickets.saveAndFlush(first);

        Ticket second = buildTicket("TCK-0001", TicketType.GENERAL, TicketStatus.RESERVED,
                new BigDecimal("120000.00"), user, event);

        assertThatThrownBy(() -> tickets.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindTicketsByUserEmailAndStatus() {
        User user = saveUser("andrea-ticket", "andrea.ticket@x.com");
        Event event = saveEvent("EV-T2", saveVenue("V-T2"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(3));
        tickets.saveAll(List.of(
                buildTicket("TCK-U1", TicketType.VIP, TicketStatus.PAID, new BigDecimal("250000.00"), user, event),
                buildTicket("TCK-U2", TicketType.GENERAL, TicketStatus.RESERVED, new BigDecimal("120000.00"), user, event)
        ));
        tickets.flush();

        assertThat(tickets.findByUserEmail("andrea.ticket@x.com")).hasSize(2);
        assertThat(tickets.findByUserEmailAndStatus("andrea.ticket@x.com", TicketStatus.PAID))
                .extracting(Ticket::getTicketCode)
                .containsExactly("TCK-U1");
    }

    @Test
    void shouldCountOnlyPaidTickets() {
        User user = saveUser("laura", "l@x.com");
        Event event = saveEvent("CMF-2026", saveVenue("VEN-SMR-01"), EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(10));

        tickets.saveAll(List.of(
                buildTicket("T1", TicketType.VIP, TicketStatus.PAID, new BigDecimal("250000.00"), user, event),
                buildTicket("T2", TicketType.GENERAL, TicketStatus.PAID, new BigDecimal("120000.00"), user, event),
                buildTicket("T3", TicketType.GENERAL, TicketStatus.RESERVED, new BigDecimal("120000.00"), user, event),
                buildTicket("T4", TicketType.VIP, TicketStatus.CANCELLED, new BigDecimal("250000.00"), user, event)
        ));
        tickets.flush();

        assertThat(tickets.countPaidByEventCode("CMF-2026")).isEqualTo(2L);
        assertThat(tickets.findPaidByEventCode("CMF-2026")).hasSize(2);
    }

    @Test
    void shouldFindTicketsForFutureEventsOrderedByEventDate() {
        User user = saveUser("future-user", "future@x.com");
        Event later = saveEvent("EV-LATER", saveVenue("V-LATER"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(20));
        Event sooner = saveEvent("EV-SOONER", saveVenue("V-SOONER"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(5));
        tickets.saveAll(List.of(
                buildTicket("T-FUT-1", TicketType.GENERAL, TicketStatus.PAID, new BigDecimal("100.00"), user, later),
                buildTicket("T-FUT-2", TicketType.GENERAL, TicketStatus.PAID, new BigDecimal("100.00"), user, sooner)
        ));
        tickets.flush();

assertThat(tickets.findForFutureEvents(LocalDateTime.now()))
                .extracting(ticket -> ticket.getEvent().getEventCode())
                .containsExactly("EV-SOONER", "EV-LATER");
    }

    @Test
    void shouldRejectNegativePrice() {
        User user = saveUser("neg-price", "neg@x.com");
        Event event = saveEvent("EV-NEG", saveVenue("V-NEG"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(3));

        Ticket negative = buildTicket("TCK-NEG", TicketType.VIP, TicketStatus.PAID,
                new BigDecimal("-1.00"), user, event);

        assertThatThrownBy(() -> tickets.saveAndFlush(negative))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

