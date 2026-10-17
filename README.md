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
- Autenticacion: `POST /auth/login` y `GET /auth/me`.

Las creaciones retornan `201`; las consultas y transiciones exitosas, `200`. Los errores usan
`ErrorResponse` y retornan `400`, `401`, `403`, `404`, `409` o `500` según corresponda.

## Seguridad (Fase 4)

La capa de seguridad usa Spring Security con JWT firmado en HS256 (JJWT) y autorizacion por
roles `USER`, `STAFF` y `ADMIN`. La API es stateless: toda peticion protegida requiere
`Authorization: Bearer <token>`.

Flujo:

1. `POST /auth/login` con `identifier` (username o email) y `password` valida las credenciales
   contra la tabla `user_credentials` (hash BCrypt) y devuelve `accessToken`, `tokenType` y
   `expiresIn`.
2. El token HS256 incluye `iss`, `sub` (id interno del usuario), `iat`, `exp` y `jti`.
3. `JwtAuthenticationFilter` valida firma, algoritmo, emisor y claims obligatorios, recarga el usuario
   y sus roles actuales por id, y solo autentica si la cuenta sigue activa.

Matriz de autorizacion:

| Endpoint | Acceso |
| --- | --- |
| `POST /auth/login`, `POST /users` | Publico |
| `GET /events/published`, `GET /events/by-artist`, `GET /events/{code}` (solo publicados), `GET /artists/{id}`, `GET /artists/by-stage-name`, `GET /artists/active`, `GET /venues/{code}`, `GET /venues/active` | Publico |
| `POST /events`, `PATCH /events/{code}/publish`, `POST /events/{code}/artists/{artistId}` | ADMIN |
| `GET /events/{code}/tickets/paid`, `PATCH /tickets/{code}/use` | STAFF o ADMIN |
| `POST /tickets` | USER; solo compra para su propia identidad |
| `GET /tickets/{code}` | Propietario, STAFF o ADMIN |
| `GET /tickets/by-user`, `PATCH /tickets/{code}/cancel` | Propietario; ADMIN puede operar sobre terceros |
| `GET /users/by-email`, `GET /users/by-username` | Propio usuario o ADMIN |
| `GET /auth/me` | Autenticado |

Ademas de los roles, `AuthorizationService` compara la propiedad con el ID persistente del principal,
no con IDs ni correos suministrados como prueba de identidad por el cliente. El registro exige una
contraseña de al menos 12 caracteres y limita el valor codificado en UTF-8 a 72 bytes para BCrypt.
Los borradores de evento se ocultan a quien no es `STAFF`/`ADMIN`. En este MVP, `STAFF` tiene acceso
global a las operaciones de tickets porque el dominio no asigna personal a eventos; producción debe
restringirlo por evento. Las respuestas `401` (no autenticado) y `403` (sin permiso) usan el mismo
`ErrorResponse`.

Las rutas publicas son explicitas; otras rutas no listadas requieren autenticacion. No hay registro
publico de roles privilegiados: `STAFF`/`ADMIN` deben provisionarse mediante un proceso
administrativo controlado. Las pruebas de integracion usan secretos y usuarios ficticios.

## Tecnologias

- Java 21
- Spring Boot 4.x
- Spring Security 7 + JJWT 0.13
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
- `V4__security_credentials_roles.sql`: crea `user_credentials` (PK/FK `user_id`) y `user_roles`
  (PK `user_id` + `role`, con `CHECK role IN ('USER','STAFF','ADMIN')`).

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
- `UserCredentialRepository.findByUserId`
- `UserRoleRepository.findRolesByUserId`

## Pruebas

`.\mvnw.cmd test` ejecuta las pruebas unitarias y las de capa web (`@WebMvcTest`), incluida la
matriz de autorizacion de seguridad (`SecurityAuthorizationMatrixTest`) que valida `401`/`403` y el
acceso por rol con la cadena real de Spring Security (usando `@WithMockUser`). Requiere
`spring-boot-starter-security-test` para que la integracion MockMvc + Spring Security funcione en
Spring Boot 4.

Las pruebas de integracion (`*IT`, `VenuePersistenceTest`) usan PostgreSQL real mediante
Testcontainers (no H2) y estan excluidas del build por defecto de Surefire; requieren Docker
activo. Incluyen `SecurityFlowIT`, que valida de extremo a extremo registro atomico (credencial
BCrypt + `ROLE_USER`), login y verificacion del JWT. Para ejecutarlas manualmente:

```powershell
.\mvnw.cmd test "-Dtest=*IT,VenuePersistenceTest"
```

Ejecutar todo:

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

pulsepass:
  security:
    jwt:
      issuer: pulsepass-api
      secret-base64: ${PULSEPASS_JWT_SECRET_BASE64}
      access-token-ttl: PT15M
  cors:
    allowed-origins: ${PULSEPASS_CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

Variables de entorno:

- `PULSEPASS_JWT_SECRET_BASE64` (obligatoria): secreto Base64 de al menos 32 bytes. La aplicacion
  falla al arrancar si falta o es demasiado corto.
- `PULSEPASS_JWT_ISSUER` (opcional, por defecto `pulsepass-api`).
- `PULSEPASS_JWT_ACCESS_TOKEN_TTL` (opcional, ISO-8601 duration; por defecto `PT15M`).
- `PULSEPASS_CORS_ALLOWED_ORIGINS` (opcional, por defecto `http://localhost:4200`).

Cada request recibe el encabezado `X-Request-ID` (se conserva solo si es válido) y ese mismo
identificador se incluye en los logs de autenticación y denegación, nunca junto con passwords,
hashes ni tokens.

Ver `.env.example` para un ejemplo.
