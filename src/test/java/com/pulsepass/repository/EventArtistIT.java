package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class EventArtistIT extends BaseIntegrationTest {
    @Autowired private EventRepository events;
    @Autowired private ArtistRepository artists;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void shouldAssociateMultipleArtistsWithoutDuplicates() {
        Venue venue = saveVenue("VEN-A1");
        Event event = saveEvent("EV-A1", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(5));

        Artist solarBeat = saveArtist("Solar Beat");
        Artist neonWaves = saveArtist("Neon Waves");
        Artist caribbeanSound = saveArtist("Caribbean Sound");

        event.getArtists().addAll(Set.of(solarBeat, neonWaves, caribbeanSound));
        events.saveAndFlush(event);

        List<Event> byArtist = events.findByArtistStageName("Solar Beat");
        assertThat(byArtist).hasSize(1);
        assertThat(byArtist.get(0).getArtists()).hasSize(3);
    }

@Test
    void shouldRejectDuplicateEventArtistPair() {
        Venue venue = saveVenue("VEN-A2");
        Event event = saveEvent("EV-A2", venue, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(5));
        Artist artist = saveArtist("Ocean Drive");
        event.getArtists().add(artist);
        events.saveAndFlush(event);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO event_artists(event_id, artist_id) VALUES (?, ?)", event.getId(), artist.getId()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectDuplicateStageName() {
        Artist first = new Artist();
        first.setStageName("Stage-Dup");
        first.setCountry("Colombia");
        first.setGenre("Rock");
        first.setActive(true);
        artists.saveAndFlush(first);

        Artist duplicate = new Artist();
        duplicate.setStageName("Stage-Dup");
        duplicate.setCountry("Peru");
        duplicate.setGenre("Pop");
        duplicate.setActive(true);

        assertThatThrownBy(() -> artists.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

