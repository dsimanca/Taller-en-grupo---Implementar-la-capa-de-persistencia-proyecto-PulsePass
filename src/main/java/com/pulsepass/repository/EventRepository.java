package com.pulsepass.repository;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByVenueCode(String venueCode);

    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.artists a
        WHERE a.stageName = :stageName
    """)
    List<Event> findByArtistStageName(@Param("stageName") String stageName);

    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.venue v
        JOIN e.artists a
        WHERE v.city = :city AND a.stageName = :stageName
    """)
    List<Event> findByCityAndArtist(@Param("city") String city, @Param("stageName") String stageName);

    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.venue v
        JOIN e.artists a
        WHERE e.status = com.pulsepass.domain.enums.EventStatus.PUBLISHED
          AND e.eventDate > :since
          AND v.city = :city
          AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
        ORDER BY e.eventDate ASC
    """)
    List<Event> findRecommended(
            @Param("since") LocalDateTime since,
            @Param("city") String city,
            @Param("artistText") String artistText);
}

