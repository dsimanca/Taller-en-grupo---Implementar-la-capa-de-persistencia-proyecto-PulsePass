package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventSearchIT extends BaseIntegrationTest {
    @Autowired private EventRepository events;

    @Test
    void shouldFindEventsByArtistWithoutDuplicates() {
        Venue venue = saveVenue("VEN-SRC-ART");
        Event first = saveEvent("EV-SRC-1", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(3));
        Event second = saveEvent("EV-SRC-2", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(4));
        Artist artist = saveArtist("Digital Pulse");
        first.getArtists().add(artist);
        second.getArtists().add(artist);
        events.saveAllAndFlush(List.of(first, second));

        assertThat(events.findByArtistStageName("Digital Pulse"))
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("EV-SRC-1", "EV-SRC-2");
    }

    @Test
    void shouldFindEventsByCityAndArtist() {
        Venue santaMarta = saveVenue("VEN-SMR-01");
        santaMarta.setCity("Santa Marta");
        Venue bogota = saveVenue("VEN-BOG-01");
        bogota.setCity("Bogota");

        Event matching = saveEvent("CMF-2026", santaMarta, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10));
        Event otherCity = saveEvent("BOG-2026", bogota, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10));
        Artist artist = saveArtist("Solar Beat");
        matching.getArtists().add(artist);
        otherCity.getArtists().add(artist);
        events.saveAllAndFlush(List.of(matching, otherCity));

        assertThat(events.findByCityAndArtist("Santa Marta", "Solar Beat"))
                .extracting(Event::getEventCode)
                .containsExactly("CMF-2026");
    }

    @Test
    void shouldFindRecommendedCaseInsensitiveOrderedByDate() {
        Venue venue = saveVenue("VEN-REC-01");
        venue.setCity("Santa Marta");
        Artist artist = saveArtist("Solar Beat");

        Event later = saveEvent("REC-LATER", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(8));
        Event sooner = saveEvent("REC-SOONER", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(2));
        Event draft = saveEvent("REC-DRAFT", venue, EventStatus.DRAFT, LocalDateTime.now().plusDays(1));
        later.getArtists().add(artist);
        sooner.getArtists().add(artist);
        draft.getArtists().add(artist);
        events.saveAllAndFlush(List.of(later, sooner, draft));

        List<Event> recommended = events.findRecommended(
                LocalDateTime.now().minusDays(1), "Santa Marta", "solar");

        assertThat(recommended)
                .extracting(Event::getEventCode)
                .containsExactly("REC-SOONER", "REC-LATER");
    }
}

