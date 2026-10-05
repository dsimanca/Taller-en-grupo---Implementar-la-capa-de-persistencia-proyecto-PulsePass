package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.mapper.VenueMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.Impl.ArtistServiceImpl;
import com.pulsepass.service.Impl.VenueServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {
    @Mock private VenueRepository venueRepository;
    @Mock private VenueMapper venueMapper;
    @Mock private ArtistRepository artistRepository;
    @Mock private ArtistMapper artistMapper;
    private VenueService venueService;
    private ArtistService artistService;

    @BeforeEach
    void setUp() {
        venueService = new VenueServiceImpl(venueRepository, venueMapper);
        artistService = new ArtistServiceImpl(artistRepository, artistMapper);
    }

    @Test
    void mapsAnExistingVenue() {
        Venue venue = new Venue();
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(venue));

        venueService.findByCode("VEN-1");

        verify(venueMapper).toResponse(venue);
    }

    @Test
    void rejectsAnUnknownVenue() {
        when(venueRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.findByCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void mapsAnExistingArtistIgnoringCase() {
        Artist artist = new Artist();
        when(artistRepository.findByStageNameIgnoreCase("solar beat")).thenReturn(Optional.of(artist));

        artistService.findByStageName("solar beat");

        verify(artistMapper).toResponse(artist);
    }

    @Test
    void rejectsAnUnknownArtist() {
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
