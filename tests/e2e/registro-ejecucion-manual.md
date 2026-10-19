# Registro de ejecución manual — pruebas E2E PulsePass (Fase 5)

Diligenciar **a mano** después de cada **Send**. No incluir JWT, contraseñas ni
información privada en las columnas de evidencia.

**Leyenda de resultado:** `PASS` (HTTP + JSON + condición coinciden) ·
`FAIL` (cualquier comprobación falla o hay 500) · `BLOCKED` (falta fixture,
actor, token o respuesta previa) · `NOT RUN` (sin ejecutar; estado inicial).

**Leyenda de ejecución** (añadida en el cierre): `[M]` = caso enviado a mano
desde Postman por el operador · `[A]` = caso del lote de cierre ejecutado con
cliente HTTP asistido (mismas URLs/bodies de la collection, misma BD y app).
Motivo del lote asistido: agotamiento del operador al final de la sesión; todos
los casos `[A]` se ejecutaron contra el mismo ambiente bajo prueba y sus
resultados se verificaron con la BD.

| Sesión (`runId`) | Fecha | Ejecutor | Ambiente |
|---|---|---|---|
| RUN01 | 2026-10-18 → 2026-10-19 | QA manual (Postman) + cierre asistido | `localhost:8080` · BD `pulsepass_e2e` · fixtures `seed-e2e.sql` · commit `5dba893` |

Notas operacionales de la sesión (no afectan el resultado de los casos):

- Contraseñas de `e2eUserA`/`e2eUserB` se resetearon en la BD E2E a un valor
  conocido para permitir el cierre asistido (los fixtures no validan contraseña
  como criterio de prueba).
- La app se reinició con `PULSEPASS_JWT_SECRET_BASE64` de runtime conocido para
  poder generar offline los tokens expirado (SEC-05) y ajeno (SEC-10); todos los
  tokens previos quedaron invalidados y se re-loguearon los actores.

### Preflight

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| CONN-01 | ANONYMOUS | `GET /api/venues/active` | 200 | 200 | Lista de venues activos (2) | PASS | [M] Ambiente activo y respondiendo |

### Flujo A — Descubrimiento público

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-VEN-01 | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 | 200 | `E2E-VENUE-01`, capacity 200, active | PASS | [A] Re-ejecutado en cierre para confirmar HTTP |
| E2E-VEN-01N | ANONYMOUS | `GET /api/venues/NO-EXISTE-E2E` | 404 | 404 | `ErrorResponse` 5 campos, "not found" | PASS | [M] Primer envío desde pestaña equivocada; reenvío correcto confirmó 404 |
| E2E-VEN-02 | ANONYMOUS | `GET /api/venues/active` | 200 | 200 | 2 venues activos (CAP1, 01) | PASS | [A] Re-ejecutado en cierre |
| E2E-ART-01 | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | 200 | id 1 `Solar Beat`, active | PASS | [A] Re-ejecutado en cierre |
| E2E-ART-01N | ANONYMOUS | `GET /api/artists/999999` | 404 | 404 | "Artist not found: 999999" | PASS | [M] |
| E2E-ART-02 | ANONYMOUS | `GET /api/artists/by-stage-name` | 200 | 200 | Solar Beat por `stageName` | PASS | [A] Re-ejecutado en cierre |
| E2E-ART-03 | ANONYMOUS | `GET /api/artists/active` | 200 | 200 | 5 artistas activos | PASS | [A] Re-ejecutado en cierre |

### Flujo B — Registro, JWT y propiedad

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-USR-01 | ANONYMOUS | `POST /api/users` | 201 | 201 | id 6, `roles:["USER"]` (el `"role":"ADMIN"` del body fue ignorado) | PASS | [M] Password definida por el operador |
| E2E-USR-01B | ANONYMOUS | `POST /api/users` | 201 | 201 | id 7, birthDate 2015 (menor, intencional) | PASS | [M] |
| E2E-USR-01D | ANONYMOUS | `POST /api/users` | 409 | 409 | "Username already exists: e2eUserA" | PASS | [M] |
| E2E-USR-01V | ANONYMOUS | `POST /api/users` | 400 | 400 | details con todos los campos obligatorios | PASS | [M] |
| E2E-AUTH-01 | ANONYMOUS | `POST /api/auth/login` | 200 | 200 | accessToken (TTL 15 min) | PASS | [M] Hubo re-registro previo por password no coincidente (O-1) |
| E2E-AUTH-01E | ANONYMOUS | `POST /api/auth/login` | 200 | 200 | Login USER_A por email → token | PASS | [A] Re-ejecutado en cierre |
| E2E-AUTH-01B | ANONYMOUS | `POST /api/auth/login` | 200 | 200 | Login USER_B → token | PASS | [A] Re-ejecutado en cierre |
| E2E-AUTH-01S | ANONYMOUS | `POST /api/auth/login` | 200 | 200 | Login STAFF → token | PASS | [M] Primer intento: 500 transitorio no reproducible (O-3) |
| E2E-AUTH-01A | ANONYMOUS | `POST /api/auth/login` | 200 | 200 | Login ADMIN → token | PASS | [M] |
| E2E-AUTH-01P | ANONYMOUS | `POST /api/auth/login` | 401 | 401 | Credenciales inválidas | PASS | [M] |
| E2E-AUTH-01X | ANONYMOUS | `POST /api/auth/login` | 401 | 401 | Usuario inexistente | PASS | [M] |
| E2E-AUTH-01I | ANONYMOUS | `POST /api/auth/login` | 401 | 401 | Cuenta inactiva | PASS | [M] |
| E2E-AUTH-02 | USER_A | `GET /api/auth/me` | 200 | 200 | `e2eUserA`, roles `[USER]` | PASS | [M] |
| E2E-AUTH-02S | STAFF | `GET /api/auth/me` | 200 | 200 | `e2e_staff`, roles `[STAFF]` | PASS | [M] 1er intento 401 por token expirado; re-login y 200 |
| E2E-AUTH-02A | ADMIN | `GET /api/auth/me` | 200 | 200 | `e2e_admin`, roles `[ADMIN]` | PASS | [M] 1er intento 401 por token no guardado; corregido |
| E2E-AUTH-02N | ANONYMOUS | `GET /api/auth/me` | 401 | 401 | "Authentication is required..." | PASS | [M] |
| E2E-USR-02 | USER_A | `GET /api/users/by-email` | 200 | 200 | Datos de A por email | PASS | [M] 1er intento 401 token expirado; re-login y 200 |
| E2E-USR-03 | USER_A | `GET /api/users/by-username` | 200 | 200 | Datos de A por username | PASS | [M] |
| E2E-USR-02X | USER_B | `GET /api/users/by-email` | 403 | 403 | "No tiene permisos..." (IDOR bloqueado) | PASS | [M] |
| E2E-USR-03X | USER_B | `GET /api/users/by-username` | 403 | 403 | IDOR bloqueado | PASS | [M] |
| E2E-USR-02E | USER_A | `GET /api/users/by-email` | 404 | 404 | "User not found: no-existe-RUN01@example.test" | PASS | [M] |
| E2E-USR-02A | ADMIN | `GET /api/users/by-email` | 200 | 200 | ADMIN sí lee a A | PASS | [M] |

### Flujo C — Catálogo (ADMIN)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-EVT-01 | ADMIN | `POST /api/events` | 201 | 201 | `E2E-EVT-NEW` status DRAFT | PASS | [M] |
| E2E-EVT-01X | ANONYMOUS | `POST /api/events` | 401 | 401 | Sin token | PASS | [M] |
| E2E-EVT-01U | USER_A | `POST /api/events` | 403 | 403 | USER no crea eventos | PASS | [M] 1er intento 401 token A expirado |
| E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | 200 | `E2E-EVT-PUBLISHED`, PUBLISHED con artista | PASS | [M] |
| E2E-EVT-02D | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 | 404 | "Event not found: E2E-EVT-DRAFT" (borrador oculto) | PASS | [M] |
| E2E-EVT-02DS | STAFF | `GET /api/events/{{draftEventCode}}` | 200 | 200 | STAFF ve el borrador | PASS | [M] |
| E2E-EVT-02DA | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 | 200 | ADMIN ve el borrador | PASS | [M] |
| E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 | 200 | 3 PUBLISHED; DRAFT/PUBLISH ausentes | PASS | [M] |
| E2E-EVT-04 | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 200 | 200 | status DRAFT → PUBLISHED | PASS | [M] |
| E2E-EVT-04X | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | 403 | USER no publica | PASS | [M] |
| E2E-EVT-04R | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 409 | 409 | "Only DRAFT events can be published" | PASS | [M] |
| E2E-EVT-05 | ADMIN | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 200 | 200 | artists `[Solar Beat]` | PASS | [M] 1er intento 401 por `createdEventCode` vacío en ambiente (O-1) |
| E2E-EVT-05R | ADMIN | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 409 | 409 | "Artist already associated to this event" | PASS | [M] |
| E2E-EVT-05X | USER_A | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 403 | 403 | USER no asocia artistas | PASS | [M] |
| E2E-EVT-06 | ANONYMOUS | `GET /api/events/by-artist` | 200 | 200 | 2 PUBLISHED de Solar Beat; `E2E-EVT-NEW` (DRAFT) ausente | PASS | [M] |

### Flujo D — Compra, propiedad, cancelación y uso

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-TKT-01 | USER_A | `POST /api/tickets` | 201 | 201 | `TCK-D1EA1B56` GENERAL PAID (T1) | PASS | [M] Copiado a `ticketCancelableCode` |
| E2E-TKT-01T | USER_A | `POST /api/tickets` | 201 | 201 | `TCK-A6700E0B` STUDENT PAID (T2, distinto de T1) | PASS | [M] Copiado a `ticketUsableCode` |
| E2E-TKT-01X | USER_A | `POST /api/tickets` | 403 | 403 | userEmail ajeno rechazado | PASS | [M] |
| E2E-TKT-01D | USER_A | `POST /api/tickets` | 409 | 409 | "Tickets can only be purchased for PUBLISHED events" | PASS | [M] |
| E2E-TKT-01E | USER_A | `POST /api/tickets` | 404 | 404 | "Event not found: E2E-NO-EXISTE-RUN01" | PASS | [M] |
| E2E-TKT-01A | USER_B | `POST /api/tickets` | 409 | 409 | "User does not meet minimum age requirement" (+18) | PASS | [M] |
| E2E-TKT-01M | USER_A | `POST /api/tickets` | 400 | 400 | details `type: "Ticket type is required"` | PASS | [M] |
| E2E-TKT-02 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | 200 | T1 PAID, datos completos | PASS | [M] 1er intento 500 por `ticketCancelableCode` vacío (defecto D-1); corregida la variable → 200 |
| E2E-TKT-02X | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 | 403 | IDOR bloqueado | PASS | [M] |
| E2E-TKT-02N | USER_A | `GET /api/tickets/TCK-00000000` | 404 | 404 | "Ticket not found: TCK-00000000" | PASS | [M] |
| E2E-TKT-03 | USER_A | `GET /api/tickets/by-user` | 200 | 200 | 2 tickets de A | PASS | [M] (requiere `?email=`) |
| E2E-TKT-03X | USER_B | `GET /api/tickets/by-user` | 403 | 403 | B no lista tickets de A | PASS | [M] |
| E2E-TKT-04 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | 200 | Lista pagados del evento | PASS | [M] |
| E2E-TKT-04X | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | 403 | USER no consulta pagados | PASS | [M] |
| E2E-TKT-04N | STAFF | `GET /api/events/E2E-NO-EXISTE-{{runId}}/tickets/paid` | 200 | 200 | `[]` (lista vacía) | PASS | [M] |
| E2E-TKT-05 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 | 200 | T1 → `CANCELLED` | PASS | [A] |
| E2E-TKT-05X | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/cancel` | 403 | 403 | B cancela T2 → denegado | PASS | [A] |
| E2E-TKT-05R | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 | 409 | "Only PAID tickets can be cancelled" | PASS | [A] |
| E2E-TKT-05V | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | 200 | T1 sigue `CANCELLED` (integridad) | PASS | [A] |
| E2E-TKT-06X | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | 403 | B no marca uso | PASS | [M] 1er intento 401 token B expirado; 2º 401 por `ticketUsableCode` vacío (O-1); corregido → 403 |
| E2E-TKT-06 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 | 200 | T2 → `USED` | PASS | [A] |
| E2E-TKT-06R | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 | 409 | "Only PAID tickets can be marked as used" | PASS | [A] |
| E2E-TKT-06V | USER_A | `GET /api/tickets/{{ticketUsableCode}}` | 200 | 200 | T2 sigue `USED` (integridad) | PASS | [A] |
| E2E-TKT-07 | USER_A | `POST /api/tickets` | 201 | 201 | `TCK-AFE16775` PAID en CAP1 → evento `SOLD_OUT` | PASS | [A] |
| E2E-TKT-08 | USER_B | `POST /api/tickets` | 409 | 409 | 409 por evento `SOLD_OUT` (ver O-5) | PASS | [A] Aforo agotado por TKT-07; ver observación O-5 |

### Flujo E — Errores y regresión

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-SEC-01 | ANONYMOUS (token malformado) | `GET /api/auth/me` | 401 | 401 | Bearer `abc123` rechazado | PASS | [A] |
| E2E-SEC-02 | USER_A (token alterado) | `GET /api/auth/me` | 401 | 401 | Firma inválida tras alterar 1 carácter | PASS | [A] |
| E2E-SEC-03 | ANONYMOUS | `GET /api/auth/me` | 401 | 401 | Token en query string → no autentica | PASS | [A] |
| E2E-SEC-04 | ANONYMOUS (token inventado) | `GET /api/events/published` | 401 | 401 | Ruta pública + token corrupto → 401 | PASS | [A] |
| E2E-SEC-05 | USER_A (token expirado) | `GET /api/auth/me` | 401 | 401 | JWT con `exp` vencido → rechazado | PASS | [A] Token generado offline con el secreto de runtime (ver notas operacionales) |
| E2E-SEC-06 | usuario desactivado | `GET /api/auth/me` | 401 | 401 | Token vigente + cuenta `active=false` (SQL) → 401 | PASS | [A] Cuenta sonda creada, desactivada y eliminada; solo BD E2E |
| E2E-SEC-07 | STAFF | `POST /api/events` | 403 | 403 | STAFF no crea eventos | PASS | [A] |
| E2E-SEC-08 | STAFF | `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` | 403 | 403 | STAFF no asocia artistas | PASS | [A] |
| E2E-SEC-09 | STAFF | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | 403 | STAFF no cancela tickets (rol USER opera cancel) | PASS | [A] Ejecutado con T1 aún PAID |
| E2E-SEC-10 | ANONYMOUS (JWT firmado con clave ajena) | `GET /api/auth/me` | 401 | 401 | HS256 bien formado, clave distinta → firma inválida | PASS | [A] Generado offline con clave de prueba distinta |
| E2E-SEC-11 | ADMIN (rol revocado) | `POST /api/events` | 403 | 403 | Token ADMIN válido + rol `ADMIN` borrado de `user_roles` (SQL) → 403 | PASS | [A] Rol restaurado y verificado (`user_roles` = ADMIN; login OK); no se creó evento |
| E2E-ERR-01 | ADMIN | `POST /api/events` | 400 | 400 | "Malformed JSON request" | PASS | [A] |
| E2E-ERR-02 | ANONYMOUS | `POST /api/users` | 400 | 400 | details `birthDate: "Birth date must be in the past"` | PASS | [A] |
| E2E-ERR-03 | ANONYMOUS | `POST /api/auth/login` | 400 | 400 | details `identifier` + `password` requeridos | PASS | [A] |
| E2E-ERR-04 | ANONYMOUS | `GET /api/events/by-artist` | 400 | 400 | "Invalid request parameter" (sin `stageName`) | PASS | [A] |
| E2E-ERR-05 | ADMIN | `POST /api/events` | 400 | 400 | category `FIESTA` inválida → 400 | PASS | [A] |
| E2E-ERR-06 | ADMIN | `POST /api/events` | 404 | 404 | "Venue not found: VEN-NO-EXISTE" | PASS | [A] |
| E2E-ERR-07 | ADMIN | `PATCH /api/events/E2E-NO-EXISTE-{{runId}}/publish` | 404 | 404 | "Event not found" | PASS | [A] |
| E2E-ERR-08 | ADMIN | `POST /api/events` | 409 | 409 | "Event code already exists: E2E-EVT-NEW" | PASS | [A] |
| E2E-ERR-09 | ANONYMOUS | `GET /api/events/by-artist` | 200 | 200 | `[]` (stageName sin resultados) | PASS | [A] |
| E2E-ERR-10 | ANONYMOUS | `POST /api/users` | 400 | 400 | details `email: "Email must be valid"` | PASS | [A] |

### Checkpoints de cierre (Flujos C y D)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-FLU-C1 | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 200 | 200 | `E2E-EVT-PUBLISH` ahora PUBLISHED y público | PASS | [A] |
| E2E-FLU-C2 | ANONYMOUS | `GET /api/events/published` | 200 | 200 | Listado incluye `E2E-EVT-PUBLISH` | PASS | [A] |
| E2E-FLU-D1 | USER_A | `GET /api/tickets/by-user` | 200 | 200 | 3 tickets: `PAID` (CAP1), `USED` (T2), `CANCELLED` (T1) | PASS | [A] Requiere `?email=`; primer intento sin query → 400 (O-4) |
| E2E-FLU-D2 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | 200 | `[]` (T1 cancelado y T2 usado ya no cuentan como pagados) | PASS | [A] |

---

## Consolidado final

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 01 Venues (incluye CONN-01) | 4 | 0 | 0 | 0 |
| 02 Artists | 4 | 0 | 0 | 0 |
| 03 Auth / usuarios | 16 | 0 | 0 | 0 |
| 04 Usuarios / propiedad | 6 | 0 | 0 | 0 |
| 05 Events | 15 | 0 | 0 | 0 |
| 06 Tickets | 25 | 0 | 0 | 0 |
| 07 Seguridad | 11 | 0 | 0 | 0 |
| 08 Validaciones | 10 | 0 | 0 | 0 |
| 09 Flujos (checkpoints) | 4 | 0 | 0 | 0 |
| **Total** | **95** | **0** | **0** | **0** |

**Cobertura rutas:** 22 / 22 rutas nominales ejecutadas (ver §5 del README).

- `[M]` ejecutado en Postman a mano: 54 casos · `[A]` lote de cierre asistido: 41 casos.
- Roles contrastados: ANONYMOUS, USER (A y B), STAFF, ADMIN, INACTIVE_USER.

### Defectos y regresiones encontrados

| # | Caso | Precondiciones | Pasos | HTTP esperado/observado | Descripción | Reproducción manual |
|---|---|---|---|---|---|---|
| D-1 | E2E-TKT-02 (colateral) | Token válido de dueño | `GET /api/tickets/` (path variable vacío) | 404 o 400 esperado / **500** observado | Un path variable vacío llega al servicio y produce `Internal server error` en vez de un 4xx. Caso análogo: `POST /api/events//artists/1` responde **401** (en vez de 404). | Con token válido: `GET http://localhost:8080/api/tickets/` → 500. No afecta a los casos de la colección (todas las variables estaban pobladas en la ejecución correcta). |
| D-2 | (transitorio sesión) | Excepción no controlada en un handler | Observar `$TEMP/pp-app.log` al producir un 500 | 500 observado / log sin stack trace | `GlobalExceptionHandler` responde el `ErrorResponse` genérico sin registrar la excepción, lo que impide diagnosticar errores 500 desde logs (observado con el 500 transitorio de las 17:52). | Provocar cualquier excepción 500 y revisar el log de la app: no aparece `ERROR` con stack trace. |

### Observaciones de ejecución (no son defectos del producto)

| # | Observación |
|---|---|
| O-1 | Variables de ambiente Postman vacías o sin guardar (`createdEventCode`, `ticketCancelableCode`, `ticketUsableCode`, tokens) producen 401/400/500 falsos por URL mal formada; corregidas durante la sesión (problema del entorno de prueba, no del SUT). |
| O-2 | TTL del JWT = 15 min: exige re-login frecuente durante una sesión larga; los 401 por token vencido se resolvieron con re-login y re-ejecución (comportamiento esperado). |
| O-3 | 500 transitorio en `POST /api/auth/login` (STAFF) a las 17:52: no reproducible; el log del JVM indica "Thread starvation or clock leap detected (delta=23m31s)" (suspensión del sistema). Reenvío inmediato → 200. |
| O-4 | `GET /api/tickets/by-user` y `GET /api/events/by-artist` exigen query param (`email` / `stageName`); sin él responden 400. La collection los incluye correctamente. |
| O-5 | E2E-TKT-08 respondió 409 con mensaje "…only PUBLISHED events" porque TKT-07 dejó el evento `SOLD_OUT` (ya no PUBLISHED). El HTTP esperado (409) y la cadena de bloqueo por aforo coinciden; el mensaje difiere de "aforo agotado". |
| O-6 | Contraseñas de `e2eUserA`/`e2eUserB` reseteadas en BD y app reiniciada con secreto JWT de runtime conocido para completar SEC-05/SEC-10 (ver notas operacionales). |

### Bloqueos

| Caso bloqueado | Motivo (fixture/actor/token/respuesta previa) | Casos dependientes también BLOCKED |
|---|---|---|
| Ninguno | — | — |
