package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class VenuePersistenceTest extends BaseIntegrationTest {
    @Autowired private VenueRepository venues;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void shouldPersistAndFindByCode() {
        Venue venue = new Venue();
        venue.setCode("VEN-SMR-01");
        venue.setName("Marina Convention Center");
        venue.setCity("Santa Marta");
        venue.setCapacity(5000);
        venue.setActive(true);
        venues.saveAndFlush(venue);

        Optional<Venue> found = venues.findByCode("VEN-SMR-01");
        assertThat(found).isPresent();
        assertThat(found.get().getCapacity()).isEqualTo(5000);
    }

    @Test
    void shouldRejectDuplicateCode() {
        Venue first = saveVenue("DUP-01");
        assertThat(first.getId()).isNotNull();

        Venue second = new Venue();
        second.setCode("DUP-01");
        second.setName("Duplicate Venue");
        second.setCity("Santa Marta");
        second.setCapacity(200);
        second.setActive(true);

        assertThatThrownBy(() -> venues.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

@Test
    void shouldRejectZeroCapacity() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO venues(code, name, city, capacity, active) VALUES ('BAD-1', 'Invalid', 'Santa Marta', 0, true)"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindEventsByVenueCodeUsingVenueRepository() {
        Venue target = saveVenue("VEN-SRC");
        saveVenue("VEN-SRC-OTHER");
        saveEvent("EV-A", target, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));
        saveEvent("EV-B", saveVenue("VEN-SRC-2"), EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1));

        assertThat(venues.findEventsByCode("VEN-SRC"))
                .extracting(Event::getEventCode)
                .containsExactly("EV-A");
    }
}

