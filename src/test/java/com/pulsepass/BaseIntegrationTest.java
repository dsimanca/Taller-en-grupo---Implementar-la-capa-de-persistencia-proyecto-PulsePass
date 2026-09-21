package com.pulsepass;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.VenueRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @Autowired protected VenueRepository venueRepository;
    @Autowired protected EventRepository eventRepository;
    @Autowired protected ArtistRepository artistRepository;
    @Autowired protected UserRepository userRepository;
    @Autowired protected TicketRepository ticketRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    protected Venue saveVenue(String code) {
        Venue venue = new Venue();
        venue.setCode(code);
        venue.setName("Venue " + code);
        venue.setCity("Santa Marta");
        venue.setAddress("Main Street 123");
        venue.setCapacity(5000);
        venue.setActive(true);
        return venueRepository.saveAndFlush(venue);
    }

    protected Event saveEvent(String eventCode, Venue venue, EventStatus status, LocalDateTime eventDate) {
        Event event = new Event();
        event.setEventCode(eventCode);
        event.setName("Event " + eventCode);
        event.setDescription("Academic test event");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(status);
        event.setEventDate(eventDate);
        event.setMinimumAge(0);
        event.setVenue(venue);
        return eventRepository.saveAndFlush(event);
    }

    protected Artist saveArtist(String stageName) {
        return artistRepository.findByStageName(stageName).orElseGet(() -> {
            Artist artist = new Artist();
            artist.setStageName(stageName);
            artist.setCountry("Colombia");
            artist.setGenre("Electronic");
            artist.setActive(true);
            return artistRepository.saveAndFlush(artist);
        });
    }

    protected User saveUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }

    protected Ticket buildTicket(
            String ticketCode,
            TicketType type,
            TicketStatus status,
            BigDecimal price,
            User user,
            Event event) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode(ticketCode);
        ticket.setType(type);
        ticket.setStatus(status);
        ticket.setPrice(price);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        return ticket;
    }
}

