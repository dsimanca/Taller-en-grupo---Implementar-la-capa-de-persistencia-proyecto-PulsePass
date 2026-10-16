# PulsePass

PulsePass es un caso academico de persistencia para una plataforma de eventos, artistas, usuarios, perfiles y tickets.

## API HTTP

Todos los endpoints usan el prefijo `/api`, JSON y respuestas de error uniformes. Los recursos principales son:

- Venues: `GET /venues/{code}` y `GET /venues/active`.
- Eventos: `POST /events`, `GET /events/{eventCode}`, `GET /events/published`,
  `PATCH /events/{eventCode}/publish`, `POST /events/{eventCode}/artists/{artistId}` y
  `GET /events/by-artist?stageName=...`.
- Artistas: `GET /artists/{id}`, `GET /artists/by-stage-name?stageName=...` y `GET /artists/active`.
- Usuarios: `POST /users`, `GET /users/by-email?email=...` y `GET /users/by-username?username=...`.
- Tickets: `POST /tickets`, `GET /tickets/{ticketCode}`, `GET /tickets/by-user?email=...`,
  `GET /events/{eventCode}/tickets/paid`, `PATCH /tickets/{ticketCode}/cancel` y
  `PATCH /tickets/{ticketCode}/use`.
- Seguridad: `POST /auth/login` y `GET /auth/me`.

Las creaciones retornan `201`; las consultas y transiciones exitosas, `200`. Los errores usan
`ErrorResponse` y retornan `400`, `401`, `403`, `404`, `409` o `500` según corresponda.

## Seguridad y autenticación

- `POST /api/users` registra una cuenta con contraseña de al menos 12 caracteres; la contraseña se almacena con BCrypt y el rol inicial es siempre `USER`.
- `POST /api/auth/login` acepta `identifier` (username o correo) y `password`; responde con un access token Bearer de 15 minutos.
- `GET /api/auth/me` devuelve la identidad y roles del principal autenticado.
- Los recursos de tickets se limitan por propietario; publicar eventos requiere `ADMIN`, y operar tickets requiere `STAFF` o `ADMIN` según la operación.
- La API es stateless y recibe JWT exclusivamente mediante `Authorization: Bearer <token>`.
- Roles disponibles: `USER`, `STAFF` y `ADMIN`. El registro público solo asigna `USER`; los roles operativos se provisionan por un proceso administrativo controlado y nunca desde el request de registro. Las pruebas usan identidades simuladas con `spring-security-test`.

Configura antes de iniciar la aplicación:

```powershell
$env:PULSEPASS_DB_PASSWORD = "<contraseña-local-de-PostgreSQL>"
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:PULSEPASS_JWT_ISSUER = "pulsepass-api"
$env:PULSEPASS_JWT_ACCESS_TOKEN_TTL = "PT15M"
$env:PULSEPASS_CORS_ALLOWED_ORIGINS = "https://app.example.com"
.\mvnw.cmd spring-boot:run
```

`PULSEPASS_JWT_SECRET_BASE64` debe contener al menos 32 bytes aleatorios codificados en Base64. No guardes el secreto en el repositorio ni lo registres en logs. Las cuentas antiguas sin credencial no pueden iniciar sesión hasta ser activadas mediante un proceso seguro; no se asigna una contraseña predeterminada.
Configura `PULSEPASS_CORS_ALLOWED_ORIGINS` como una lista separada por comas de orígenes explícitos. En producción, publica la API únicamente detrás de HTTPS; el request ID de respuesta permite correlacionar eventos de autenticación y denegaciones sin registrar credenciales.
`.env.example` enumera las variables requeridas, pero la aplicación no carga archivos `.env` automáticamente: expórtalas en el entorno antes de iniciarla.

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
- `V4__security_credentials_roles.sql`: agrega credenciales BCrypt y roles asociados a usuarios.

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
