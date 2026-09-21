package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class EventRepositoryIT extends BaseIntegrationTest {
    @Autowired private EventRepository events;

    @Test
    void shouldFindPublishedEventsOrderedByDate() {
        Venue venue = saveVenue("VEN-01");
        saveEvent("E1", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(2));
        saveEvent("E2", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));
        saveEvent("E3", venue, EventStatus.DRAFT, LocalDateTime.now().plusDays(3));

        List<Event> published = events.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(published).extracting(Event::getEventCode).containsExactly("E2", "E1");
    }

    @Test
    void shouldFindEventByCodeWithVenue() {
        Venue venue = saveVenue("VEN-02");
        saveEvent("CMF-2026", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10));

        Event found = events.findByEventCode("CMF-2026").orElseThrow();
        assertThat(found.getVenue().getCode()).isEqualTo("VEN-02");
    }

@Test
    void shouldFindEventsByVenueCode() {
        Venue targetVenue = saveVenue("VEN-TARGET");
        Venue otherVenue = saveVenue("VEN-OTHER");
        saveEvent("EV-TARGET", targetVenue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));
        saveEvent("EV-OTHER", otherVenue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));

        assertThat(events.findByVenueCode("VEN-TARGET"))
                .extracting(Event::getEventCode)
                .containsExactly("EV-TARGET");
    }

    @Test
    void shouldPersistStreamingUrl() {
        Venue venue = saveVenue("VEN-STREAM");
        Event event = saveEvent("EV-STREAM", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(2));
        event.setStreamingUrl("https://stream.example.com/live");
        events.saveAndFlush(event);

        Event found = events.findByEventCode("EV-STREAM").orElseThrow();
        assertThat(found.getStreamingUrl()).isEqualTo("https://stream.example.com/live");
    }

    @Test
    void shouldRejectDuplicateEventCode() {
        Venue firstVenue = saveVenue("VEN-DUP-1");
        Venue secondVenue = saveVenue("VEN-DUP-2");
        saveEvent("DUP-EVT", firstVenue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));

        Event duplicate = new Event();
        duplicate.setEventCode("DUP-EVT");
        duplicate.setName("Duplicate Event");
        duplicate.setDescription("should be rejected");
        duplicate.setCategory(EventCategory.MUSIC);
        duplicate.setStatus(EventStatus.PUBLISHED);
        duplicate.setEventDate(LocalDateTime.now().plusDays(1));
        duplicate.setMinimumAge(0);
        duplicate.setVenue(secondVenue);

        assertThatThrownBy(() -> events.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

