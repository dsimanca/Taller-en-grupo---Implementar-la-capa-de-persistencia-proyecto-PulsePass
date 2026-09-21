package com.pulsepass.repository;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.model.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserEmail(String email);

    List<Ticket> findByUserEmailAndStatus(String email, TicketStatus status);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.event.eventCode = :eventCode
          AND t.status = com.pulsepass.domain.enums.TicketStatus.PAID
    """)
    List<Ticket> findPaidByEventCode(@Param("eventCode") String eventCode);

    @Query("""
        SELECT COUNT(t) FROM Ticket t
        WHERE t.event.eventCode = :eventCode
          AND t.status = com.pulsepass.domain.enums.TicketStatus.PAID
    """)
    long countPaidByEventCode(@Param("eventCode") String eventCode);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.event.eventDate > :after
        ORDER BY t.event.eventDate ASC
    """)
    List<Ticket> findForFutureEvents(@Param("after") LocalDateTime after);
}

