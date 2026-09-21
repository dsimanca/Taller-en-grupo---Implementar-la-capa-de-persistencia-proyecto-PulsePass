package com.pulsepass.repository;

import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByCode(String code);

    @Query("SELECT e FROM Event e WHERE e.venue.code = :venueCode")
    List<Event> findEventsByCode(@Param("venueCode") String venueCode);
}

