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
En despliegues reales, el ingress o proxy de confianza debe exigir HTTPS; el access token es de vida
corta y no hay revocacion inmediata/denylist en este MVP, por lo que cerrar sesion en el cliente no
invalida tokens emitidos antes de su expiracion.

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
      access-token-ttl: ${PULSEPASS_JWT_ACCESS_TOKEN_TTL:PT15M}
  cors:
    allowed-origins: ${PULSEPASS_CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

Variables de entorno:

- `PULSEPASS_DB_PASSWORD` (obligatoria): contrasena de PostgreSQL; no guardar valores reales en
  archivos versionados.
- `PULSEPASS_DB_URL` y `PULSEPASS_DB_USERNAME` (opcionales para desarrollo local).
- `PULSEPASS_JWT_SECRET_BASE64` (obligatoria): secreto Base64 de al menos 32 bytes. La aplicacion
  falla al arrancar si falta o es demasiado corto.
- `PULSEPASS_JWT_ISSUER` (opcional, por defecto `pulsepass-api`).
- `PULSEPASS_JWT_ACCESS_TOKEN_TTL` (opcional, ISO-8601 duration; por defecto `PT15M`).
- `PULSEPASS_CORS_ALLOWED_ORIGINS` (opcional, por defecto `http://localhost:4200`).

Cada request recibe el encabezado `X-Request-ID` (se conserva solo si es válido) y ese mismo
identificador se incluye en los logs de autenticación y denegación, nunca junto con passwords,
hashes ni tokens.

Ver `.env.example` para un ejemplo.

## Cómo ejecutar todo el proyecto

### Requisitos previos
- Java 21 y Maven (incluido wrapper `mvnw.cmd` / `mvnw`)
- PostgreSQL en ejecución (por defecto `localhost:5432`, base `pulsepass`, usuario `pulsepass`)
- Node.js 22.22+ y npm

### 1. Base de datos (primera vez)
Crea la base y el usuario en PostgreSQL (ajusta la contraseña). Ejemplo con `psql`:

```sql
CREATE ROLE pulsepass WITH LOGIN PASSWORD 'tu_clave_db';
CREATE DATABASE pulsepass OWNER pulsepass;
```

Flyway crea/migra las tablas al iniciar el backend. La migración `V2` solo siembra 5 artistas (no hay sedes, eventos ni usuarios).

### 2. Backend (API REST, puerto 8080)
Ejecutar en una terminal desde la **raíz del proyecto** (`pulsepassFinal`). Son obligatorias `PULSEPASS_DB_PASSWORD` y `PULSEPASS_JWT_SECRET_BASE64` (Base64 estándar, >= 32 bytes, sin `-` ni `_`). Las variables `$env:` / `export` valen para la ventana actual.

**PowerShell (Windows):**
```powershell
$env:PULSEPASS_DB_URL = "jdbc:postgresql://localhost:5432/pulsepass"
$env:PULSEPASS_DB_USERNAME = "pulsepass"
$env:PULSEPASS_DB_PASSWORD = "tu_clave_db"

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes); $rng.Dispose()
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)

.\mvnw.cmd spring-boot:run
```

**Bash / Linux / macOS / Git Bash:**
```bash
export PULSEPASS_DB_URL="jdbc:postgresql://localhost:5432/pulsepass"
export PULSEPASS_DB_USERNAME="pulsepass"
export PULSEPASS_DB_PASSWORD="tu_clave_db"
export PULSEPASS_JWT_SECRET_BASE64="$(openssl rand -base64 32)"

./mvnw spring-boot:run
```

El servidor queda en `http://localhost:8080`. Deja esta terminal abierta.

### 3. Frontend (Vite, puerto 5173)
En **otra terminal**:

**PowerShell (Windows):**
```powershell
cd pulsepass-web
Copy-Item .env.example .env   # si no existe (VITE_API_BASE_URL=/api)
npm.cmd install               # solo la primera vez
npm.cmd run dev
```

**Bash / Linux / macOS / Git Bash:**
```bash
cd pulsepass-web
cp .env.example .env          # si no existe (VITE_API_BASE_URL=/api)
npm install                   # solo la primera vez
npm run dev
```

Abre la URL que muestre Vite (normalmente `http://localhost:5173`). El proxy de desarrollo reenvía `/api` al backend.

### 4. Verificación rápida
- Backend: `http://localhost:8080/api/artists/active` (debe listar los 5 artistas sembrados) y `http://localhost:8080/api/events/published`.
- Frontend: `http://localhost:5173` con el backend en ejecución.

### 5. Datos iniciales (opcional)
- Solo hay artistas sembrados. La cartelera muestra eventos con estado `PUBLISHED`.
- Para crear eventos necesitas una sede en BD, por ejemplo:
  ```sql
  INSERT INTO venues (code, name, city, address, capacity, active)
  VALUES ('VEN-SMR-01', 'Marina Convention Center', 'Santa Marta', 'Centro', 5000, true);
  ```
- Regístrate en `/registro` (rol `USER`). Para administrar eventos, asigna rol `ADMIN` a tu usuario en BD:
  ```sql
  -- consulta el id: SELECT id, username FROM users;
  INSERT INTO user_roles (user_id, role) VALUES (<ID_USUARIO>, 'ADMIN');
  ```
  Luego inicia sesión y usa `/admin/eventos` para crear y publicar eventos.

### 6. Pruebas
- Backend (raíz): `.\mvnw.cmd test` (Windows) / `./mvnw test` (bash).
- Frontend (`pulsepass-web/`): `npm.cmd test` (Windows) / `npm test` (bash).
