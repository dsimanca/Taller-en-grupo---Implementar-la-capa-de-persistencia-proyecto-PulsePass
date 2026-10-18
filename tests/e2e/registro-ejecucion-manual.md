# Registro de ejecuciÃ³n manual â€” pruebas E2E PulsePass (Fase 5)

Diligenciar **a mano** despuÃ©s de cada **Send**. No incluir JWT, contraseÃ±as ni
informaciÃ³n privada en las columnas de evidencia.

**Leyenda de resultado:** `PASS` (HTTP + JSON + condiciÃ³n coinciden) Â·
`FAIL` (cualquier comprobaciÃ³n falla o hay 500) Â· `BLOCKED` (falta fixture,
actor, token o respuesta previa) Â· `NOT RUN` (sin ejecutar; estado inicial).

| SesiÃ³n (`runId`) | Fecha | Ejecutor | Ambiente |
|---|---|---|---|
| _pendiente_ | _pendiente_ | _pendiente_ | _pendiente_ |


### Preflight

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| CONN-01 | ANONYMOUS | `GET /api/venues/active` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Flujo A â€” Descubrimiento pÃºblico

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-VEN-01 | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-VEN-01N | ANONYMOUS | `GET /api/venues/NO-EXISTE-E2E` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-VEN-02 | ANONYMOUS | `GET /api/venues/active` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ART-01 | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ART-01N | ANONYMOUS | `GET /api/artists/999999` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ART-02 | ANONYMOUS | `GET /api/artists/by-stage-name` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ART-03 | ANONYMOUS | `GET /api/artists/active` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Flujo B â€” Registro, JWT y propiedad

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-USR-01 | ANONYMOUS | `POST /api/users` | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-01B | ANONYMOUS | `POST /api/users` | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-01D | ANONYMOUS | `POST /api/users` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-01V | ANONYMOUS | `POST /api/users` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01 | ANONYMOUS | `POST /api/auth/login` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01E | ANONYMOUS | `POST /api/auth/login` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01B | ANONYMOUS | `POST /api/auth/login` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01S | ANONYMOUS | `POST /api/auth/login` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01A | ANONYMOUS | `POST /api/auth/login` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01P | ANONYMOUS | `POST /api/auth/login` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01X | ANONYMOUS | `POST /api/auth/login` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-01I | ANONYMOUS | `POST /api/auth/login` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-02 | USER_A | `GET /api/auth/me` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-02S | STAFF | `GET /api/auth/me` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-02A | ADMIN | `GET /api/auth/me` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-AUTH-02N | ANONYMOUS | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-02 | USER_A | `GET /api/users/by-email` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-03 | USER_A | `GET /api/users/by-username` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-02X | USER_B | `GET /api/users/by-email` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-03X | USER_B | `GET /api/users/by-username` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-02E | USER_A | `GET /api/users/by-email` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-USR-02A | ADMIN | `GET /api/users/by-email` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Flujo C â€” CatÃ¡logo (ADMIN)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-EVT-01 | ADMIN | `POST /api/events` | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-01X | ANONYMOUS | `POST /api/events` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-01U | USER_A | `POST /api/events` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-02D | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-02DS | STAFF | `GET /api/events/{{draftEventCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-02DA | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-04 | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-04X | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-04R | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-05 | ADMIN | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-05R | ADMIN | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-05X | USER_A | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-EVT-06 | ANONYMOUS | `GET /api/events/by-artist` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Flujo D â€” Compra, propiedad, cancelaciÃ³n y uso

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-TKT-01 | USER_A | `POST /api/tickets` | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01T | USER_A | `POST /api/tickets` | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01X | USER_A | `POST /api/tickets` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01D | USER_A | `POST /api/tickets` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01E | USER_A | `POST /api/tickets` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01A | USER_B | `POST /api/tickets` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-01M | USER_A | `POST /api/tickets` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-02 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-02X | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-02N | USER_A | `GET /api/tickets/TCK-00000000` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-03 | USER_A | `GET /api/tickets/by-user` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-03X | USER_B | `GET /api/tickets/by-user` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-04 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-04X | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-04N | STAFF | `GET /api/events/E2E-NO-EXISTE-{{runId}}/tickets/paid` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-05 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-05X | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/cancel` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-05R | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-05V | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-06X | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-06 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-06R | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-06V | USER_A | `GET /api/tickets/{{ticketUsableCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-07 | USER_A | POST /api/tickets | 201 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-TKT-08 | USER_B | POST /api/tickets | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Flujo E â€” Errores y regresiÃ³n

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-SEC-01 | ANONYMOUS (token malformado) | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-02 | USER_A (token alterado) | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-03 | ANONYMOUS | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-04 | ANONYMOUS (token inventado) | `GET /api/events/published` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-05 | USER_A (token expirado) | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-06 | usuario desactivado | `GET /api/auth/me` | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-07 | STAFF | `POST /api/events` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-08 | STAFF | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-09 | STAFF | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-10 | ANONYMOUS (JWT firmado con clave ajena) | GET /api/auth/me | 401 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-SEC-11 | ADMIN (rol revocado) | POST /api/events | 403 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-01 | ADMIN | `POST /api/events` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-02 | ANONYMOUS | `POST /api/users` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-03 | ANONYMOUS | `POST /api/auth/login` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-04 | ANONYMOUS | `GET /api/events/by-artist` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-05 | ADMIN | `POST /api/events` | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-06 | ADMIN | `POST /api/events` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-07 | ADMIN | `PATCH /api/events/E2E-NO-EXISTE-{{runId}}/publish` | 404 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-08 | ADMIN | `POST /api/events` | 409 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-09 | ANONYMOUS | `GET /api/events/by-artist` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-ERR-10 | ANONYMOUS | POST /api/users | 400 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

### Checkpoints de cierre (Flujos C y D)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observaciÃ³n |
|---|---|---|---|---|---|---|---|
| E2E-FLU-C1 | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-FLU-C2 | ANONYMOUS | `GET /api/events/published` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-FLU-D1 | USER_A | `GET /api/tickets/by-user` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |
| E2E-FLU-D2 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | _pendiente_ | _pendiente_ | NOT RUN | _pendiente_ |

---

## Consolidado final

| MÃ³dulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 01 Venues | 0 | 0 | 0 | 0 |
| 02 Artists | 0 | 0 | 0 | 0 |
| 03 Auth / usuarios | 0 | 0 | 0 | 0 |
| 04 Usuarios / propiedad | 0 | 0 | 0 | 0 |
| 05 Events | 0 | 0 | 0 | 0 |
| 06 Tickets | 0 | 0 | 0 | 0 |
| 07 Seguridad | 0 | 0 | 0 | 0 |
| 08 Validaciones | 0 | 0 | 0 | 0 |
| 09 Flujos (checkpoints) | 0 | 0 | 0 | 0 |
| **Total** | **0** | **0** | **0** | **90** |

**Cobertura rutas:** __ / 22 rutas nominales ejecutadas.

### Defectos y regresiones encontrados

| # | Caso | Precondiciones | Pasos | HTTP esperado/observado | DescripciÃ³n | ReproducciÃ³n manual |
|---|---|---|---|---|---|---|
| 1 | _pendiente_ | | | | | |

### Bloqueos

| Caso bloqueado | Motivo (fixture/actor/token/respuesta previa) | Casos dependientes tambiÃ©n BLOCKED |
|---|---|---|
| _pendiente_ | | |
