package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.Impl.EventServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock private EventRepository eventRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private EventMapper eventMapper;
    private EventService service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(eventRepository, venueRepository, artistRepository, eventMapper);
    }

    @Test
    void findsAnExistingEvent() {
        Event event = event("EV-1", EventStatus.DRAFT);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));

        service.findByCode("EV-1");

        verify(eventMapper).toResponse(event);
    }

    @Test
    void rejectsAnUnknownEvent() {
        when(eventRepository.findByEventCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createsAValidDraftEvent() {
        Venue venue = venue(true);
        CreateEventRequest request = request(LocalDateTime.now().plusDays(1));
        when(eventRepository.existsByEventCode(request.eventCode())).thenReturn(false);
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getValue().getVenue()).isSameAs(venue);
    }

    @Test
    void rejectsCreationWhenVenueDoesNotExist() {
        CreateEventRequest request = request(LocalDateTime.now().plusDays(1));
        when(eventRepository.existsByEventCode(request.eventCode())).thenReturn(false);
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void rejectsCreationInAnInactiveVenue() {
        CreateEventRequest request = request(LocalDateTime.now().plusDays(1));
        when(eventRepository.existsByEventCode(request.eventCode())).thenReturn(false);
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(venue(false)));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void rejectsCreationWithAPastDate() {
        CreateEventRequest request = request(LocalDateTime.now().minusMinutes(1));
        when(eventRepository.existsByEventCode(request.eventCode())).thenReturn(false);
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(venue(true)));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publishesADraftEvent() {
        Event event = event("EV-1", EventStatus.DRAFT);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        service.publish("EV-1");

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    void rejectsPublishingACancelledEventWithoutSavingIt() {
        Event event = event("EV-1", EventStatus.CANCELLED);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish("EV-1")).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(eq(event));
    }

    @Test
    void rejectsAddingTheSameArtistTwice() {
        Event event = event("EV-1", EventStatus.DRAFT);
        Artist artist = new Artist();
        event.getArtists().add(artist);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> service.addArtist("EV-1", 1L)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(event);
    }

    private CreateEventRequest request(LocalDateTime date) {
        return new CreateEventRequest("EV-1", "Event", "Description", EventCategory.MUSIC, date, 18, "VEN-1");
    }

    private Event event(String code, EventStatus status) {
        Event event = new Event();
        event.setEventCode(code);
        event.setStatus(status);
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setVenue(venue(true));
        return event;
    }

    private Venue venue(boolean active) {
        Venue venue = new Venue();
        venue.setCode("VEN-1");
        venue.setActive(active);
        venue.setCapacity(10);
        return venue;
    }
}
