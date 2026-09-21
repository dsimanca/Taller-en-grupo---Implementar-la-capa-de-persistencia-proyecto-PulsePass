# PulsePass

PulsePass es un caso academico de persistencia para una plataforma de eventos, artistas, usuarios, perfiles y tickets.

## Tecnologias

- Java 21
- Spring Boot 4.x
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Testcontainers
- Maven

## Modelo de dominio

El modelo implementa las relaciones requeridas por el PRD:

- `Venue` 1:N `Event`
- `Event` N:M `Artist` mediante `event_artists`
- `User` 1:1 `UserProfile`
- `User` 1:N `Ticket`
- `Event` 1:N `Ticket`

`Ticket` es una entidad propia porque contiene datos de negocio: `ticketCode`, `type`, `price`, `status` y `purchaseDate`.

## Migraciones

Flyway es el responsable de crear y evolucionar el esquema:

- `V1__create_schema.sql`: crea `venues`, `events`, `artists`, `event_artists`, `users`, `user_profiles` y `tickets` con PK, FK, UNIQUE, CHECK e indices.
- `V2__insert_initial_artists.sql`: inserta artistas iniciales de prueba.
- `V3__add_streaming_url_to_event.sql`: agrega `streaming_url VARCHAR(500)` nullable a `events`.

Hibernate se ejecuta en modo `validate`, por lo que no crea ni modifica tablas.

## Consultas principales

Los repositorios usan `JpaRepository`, Query Methods y JPQL segun el caso:

- `VenueRepository.findByCode`
- `VenueRepository.findEventsByCode`
- `EventRepository.findByEventCode`
- `EventRepository.findByStatusOrderByEventDateAsc`
- `EventRepository.findByVenueCode`
- `EventRepository.findByArtistStageName`
- `EventRepository.findByCityAndArtist`
- `EventRepository.findRecommended`
- `UserRepository.findByEmailIgnoreCase`
- `TicketRepository.findByUserEmail`
- `TicketRepository.findByUserEmailAndStatus`
- `TicketRepository.findPaidByEventCode`
- `TicketRepository.countPaidByEventCode`
- `TicketRepository.findForFutureEvents`

## Pruebas

Las pruebas de integracion usan PostgreSQL real mediante Testcontainers. No se usa H2.

Ejecutar:

```bash
./mvnw clean test
```

En Windows:

```powershell
.\mvnw.cmd clean test
```

## Configuracion clave

`src/main/resources/application.yml` define:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```
##gracias

