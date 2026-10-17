package com.pulsepass.service.Impl;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.AuthorizationService;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final AuthorizationService authorizationService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             TicketMapper ticketMapper,
                             AuthorizationService authorizationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        // BR-TICKET-001 + BR-TICKET-002: usuario existe y activo
        User user = userRepository.findByEmailIgnoreCase(request.userEmail()).orElse(null);
        authorizationService.requirePurchaseForSelf(user == null ? null : user.getId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + request.userEmail());
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("Inactive user cannot purchase tickets");
        }

        // BR-TICKET-003 + BR-TICKET-004: evento existe y PUBLISHED
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Tickets can only be purchased for PUBLISHED events");
        }

        // BR-TICKET-005: fecha futura
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot purchase tickets for past events");
        }

        // BR-TICKET-006: edad minima
        if (event.getMinimumAge() > 0) {
            if (user.getProfile() == null || user.getProfile().getBirthDate() == null) {
                throw new BusinessRuleException("User birth date is required for age-restricted events");
            }

            long ageAtEvent = Period.between(
                    user.getProfile().getBirthDate(),
                    event.getEventDate().toLocalDate()
            ).getYears();

            if (ageAtEvent < event.getMinimumAge()) {
                throw new BusinessRuleException("User does not meet minimum age requirement");
            }
        }

        // BR-TICKET-007: capacidad
        long paidCount = ticketRepository.countByEventEventCodeAndStatus(
                event.getEventCode(), TicketStatus.PAID);

        if (paidCount >= event.getVenue().getCapacity()) {
            throw new BusinessRuleException("Event has reached maximum capacity");
        }

        // BR-TICKET-009: calcular precio (BigDecimal, nunca negativo)
        BigDecimal price = calculatePrice(request.type());

        // Crear ticket (estado inicial PAID segun PRD simplificado)
        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setType(request.type());
        ticket.setPrice(price);
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);

        Ticket saved = ticketRepository.save(ticket);

        // BR-TICKET-008: si se completo la capacidad -> SOLD_OUT
        if (paidCount + 1 == event.getVenue().getCapacity()) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(saved);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
        // Propietario, o STAFF/ADMIN en operacion autorizada.
        authorizationService.requireTicketOwnerOrPrivileged(ticket.getUser().getId());
        return ticketMapper.toResponse(ticket);
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        // La identidad se compara por ID persistente, no por el correo suministrado.
        Long requestedUserId = userRepository.findByEmailIgnoreCase(email)
                .map(User::getId)
                .orElse(null);
        authorizationService.requireSelfOrAdmin(requestedUserId);
        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        authorizationService.requireStaffOrAdmin();
        return ticketRepository.findPaidByEventCode(eventCode)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));

        // Fase 4: propiedad antes de mutar estado. Propietario o ADMIN autorizado.
        authorizationService.requireTicketOwnerOrAdmin(ticket.getUser().getId());

        // BR-TICKET-010 + BR-TICKET-011: solo PAID
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be cancelled");
        }

        // BR-TICKET-012: no cancelar despues de la fecha del evento
        if (!ticket.getEvent().getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot cancel ticket after event date");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        authorizationService.requireStaffOrAdmin();

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));

        // BR-TICKET-013: solo PAID
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be marked as used");
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    // Estrategia de precios encapsulada
    private BigDecimal calculatePrice(TicketType type) {
        BigDecimal base = new BigDecimal("100000");
        return switch (type) {
            case GENERAL  -> base;
            case STUDENT  -> base.multiply(new BigDecimal("0.70"));
            case VIP      -> base.multiply(new BigDecimal("2.50"));
            case BACKSTAGE -> base.multiply(new BigDecimal("4.00"));
        };
    }
}
