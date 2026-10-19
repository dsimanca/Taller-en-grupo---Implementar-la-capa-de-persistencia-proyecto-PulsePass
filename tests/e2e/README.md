# PulsePass — Pruebas E2E manuales con Postman (Fase 5)

Guía de ejecución de la colección de pruebas HTTP de caja negra contra la
aplicación real (Spring Boot → filtros de seguridad → controllers → services →
PostgreSQL). **Sin mocks, sin scripts de Postman, sin Collection Runner:** cada
solicitud se envía con **Send** y el resultado se compara visualmente y se
registra a mano.

| Entregable | Archivo |
|---|---|
| Colección Postman v2.1 (sin scripts) | `postman/PulsePass-Manual-E2E.postman_collection.json` |
| Ambiente parametrizable (sin secretos) | `postman/PulsePass-local.postman_environment.json` |
| Fixtures de prueba | `fixtures/seed-e2e.sql` + `fixtures/README.md` |
| Checklist de ejecución | `checklist-e2e-manual.md` |
| Bitácora de resultados | `registro-ejecucion-manual.md` |
| Política 403/404 y cobertura | secciones 4 y 5 de este README |

---

## 1. Preparación del ambiente

### 1.1 Base de datos dedicada

Crear (o recrear) una BD **exclusiva** de pruebas — nunca tocar desarrollo, QA
ni producción:

```sql
CREATE DATABASE pulsepass_e2e;
```

### 1.2 Variables de la aplicación

La aplicación necesita estas variables de entorno (ver `.env.example` en la
raíz del proyecto):

| Variable | Ejemplo | Notas |
|---|---|---|
| `PULSEPASS_DB_URL` | `jdbc:postgresql://localhost:5432/pulsepass_e2e` | apunta a la BD E2E |
| `PULSEPASS_DB_USERNAME` / `PULSEPASS_DB_PASSWORD` | los de esa BD | obligatorias |
| `PULSEPASS_JWT_SECRET_BASE64` | clave aleatoria **≥ 32 bytes** en Base64 | exclusiva del ambiente E2E; **nunca** incluirla en la colección, capturas ni reportes |
| `PULSEPASS_JWT_ISSUER` | `pulsepass-api` | coincide con `{{expectedIssuer}}` |
| `PULSEPASS_JWT_ACCESS_TOKEN_TTL` | `PT15M` | = `{{expectedTokenTtlSeconds}}` = 900 s |

Ejecutar sobre `localhost` (o HTTPS); no transmitir credenciales reales por
HTTP público.

### 1.3 Arranque y migraciones

```powershell
.\mvnw.cmd spring-boot:run
```

Flyway aplica `V1..V4` automáticamente al arrancar (nunca reescribir su
historial). Luego aplicar los fixtures (sección 1.4). Verificar que arranca sin
errores de esquema (`ddl-auto: validate`).

### 1.4 Fixtures

```powershell
psql $env:PULSEPASS_DB_URL -U $env:PULSEPASS_DB_USERNAME -f tests\e2e\fixtures\seed-e2e.sql
```

Crea el venue `E2E-VENUE-01`, los eventos `E2E-EVT-DRAFT`, `E2E-EVT-PUBLISHED`,
`E2E-EVT-PUBLISH` y `E2E-EVT-AGE18`, y las cuentas privilegiadas
`e2e_admin` / `e2e_staff` / `e2e_inactive` (provisionadas **fuera de HTTP**, con
BCrypt). Detalle y credenciales de prueba en [`fixtures/README.md`](fixtures/README.md).

### 1.5 Importación en Postman

1. **Import** → seleccionar `postman/PulsePass-Manual-E2E.postman_collection.json`
   y `postman/PulsePass-local.postman_environment.json`.
2. En el selector de ambiente (esquina superior derecha) elegir **PulsePass-local**.
3. Abrir el ambiente → pestaña **Secret local** y completar **a mano**:
   `userAPassword`, `userBPassword`, `staffPassword`, `adminPassword`
   (valores en `fixtures/README.md`).
4. Confirmar `baseUrl` (`http://localhost:8080`) y elegir un `runId` único
   (p. ej. `RUN01`) para esta sesión.

> Esta colección **no** contiene Pre-request Scripts, Tests ni `pm.*`. Las
> pestañas de scripts deben permanecer vacías. Los tokens **no** se capturan
> automáticamente: se copian a mano (sección 3).

### 1.6 Preflight

Enviar `00 Preparación / conectividad → CONN-01 GET /api/venues/active`. Si no responde `200`,
**todos** los casos dependientes quedan `BLOCKED` (no `FAIL`) hasta restablecer
el ambiente.

---

## 2. Orden de ejecución (Flujos A–E)

Seguir el orden de `checklist-e2e-manual.md`; la carpeta **09 Flujos de
negocio** de la colección documenta la secuencia completa.

| Flujo | Contenido | Carpetas |
|---|---|---|
| **A — Descubrimiento público** | venues y artistas, listados públicos, visibilidad de borradores sin token | `00`, `01`, `02` + `E2E-EVT-03/02/02D/06` |
| **B — Registro y JWT** | registrar USER_A y USER_B, 4 logins, copiar tokens, `/me`, credenciales inválidas | `03` |
| **C — Catálogo (ADMIN)** | crear → asociar artista → publicar → verificar visibilidad pública; negativos 401/403 | `05` + `E2E-FLU-C1/C2` |
| **D — Tickets** | comprar T1/T2, propiedad y denegaciones **antes** de mutar, cancelar T1, usar T2 con STAFF | `06` + `E2E-FLU-D1/D2` |
| **E — Errores y regresión** | 400/401/403/404/409, `ErrorResponse`, JWT manipulados | `07`, `08` |

Reglas de secuencia importantes:

1. **Denegaciones antes que mutaciones:** ejecutar los IDOR de lectura/cancelación
   (`E2E-TKT-02X`, `03X`, `05X`, `06X`, `E2E-SEC-09`) **antes** de cancelar o
   usar tickets, para que la prueba de integridad no sea ambigua.
2. **Un ticket por transición irreversible:** T1 (`ticketCancelableCode`) se
   cancela; T2 (`ticketUsableCode`) se usa. No reutilizar T1 para `use`.
3. `E2E-EVT-04` (publicar) consume el fixture `E2E-EVT-PUBLISH`: una sola vez
   por sesión; re-aplicar el seed para repetirlo.
4. Antes de cada request protegida, verificar en el panel **Authorization**
   qué token usa y si sigue vigente (TTL 15 min). Si expiró: repetir el login
   correspondiente y volver a copiar el token.

---

## 3. Copia manual de identificadores y tokens

Tras cada login, copiar **a mano** el valor de `accessToken` a la variable del
actor (ambiente → valor local; **nunca** exportar ni adjuntar en evidencias):

| Login | Variable de destino |
|---|---|
| `E2E-AUTH-01` (USER_A) | `userAToken` |
| `E2E-AUTH-01B` (USER_B) | `userBToken` |
| `E2E-AUTH-01S` (STAFF) | `staffToken` |
| `E2E-AUTH-01A` (ADMIN) | `adminToken` |

Códigos que también se copian a mano:

| Origen | Variable |
|---|---|
| `GET /api/artists/by-stage-name` → `id` | `artistId` |
| `POST /api/events` → `eventCode` | `createdEventCode` |
| `POST /api/tickets` (T1) → `ticketCode` | `ticketCancelableCode` |
| `POST /api/tickets` (T2) → `ticketCode` | `ticketUsableCode` |

---

## 4. Política de denegación documentada (403 vs 404)

**Decisión explícita** (`{{expectedForeignResourceStatus}} = 403`):

| Escenario | Código | Casos |
|---|---|---|
| Recurso **ajeno existente** (tickets, compra, usuarios) | **403** | `TKT-02X/03X/05X`, `USR-02X/03X`, `TKT-01X` |
| Recurso **inexistente** (códigos, IDs) | **404** | `VEN-01N`, `ART-01N`, `TKT-02N`, `ERR-06/07` |
| **Borrador de evento** visto por anónimo/USER | **404** | `EVT-02D` (anti-filtración; STAFF/ADMIN reciben 200) |
| `GET /api/users/by-email\|by-username` **inexistente** | **404** | `USR-02E` — asimetría documentada: 404 = no existe, 403 = existe y es ajeno |
| Rol insuficiente en URL (ADMIN/OPS/USER) | **403** | `EVT-01U/04X/05X`, `TKT-04X/06X` |
| Sin token o token inválido/expirado | **401** | carpeta `07`, `AUTH-02N` |
| Reglas de negocio (estado, capacidad, edad, duplicados) | **409** | `TKT-01D/01A/05R/06R`, `EVT-04R/05R`, `ERR-08` |
| Validaciones de payload | **400** | carpeta `08` |

Un `404` no se acepta como sustituto silencioso de un `403`: cada caso de la
colección declara su código esperado en la descripción.

---

## 5. Cobertura: 22 rutas base

20 rutas de negocio + 2 de autenticación, todas presentes en la colección:

| # | Método | Ruta | Casos (ejemplos) |
|---|---|---|---|
| 1 | GET | `/api/venues/{code}` | `E2E-VEN-01`, `E2E-VEN-01N` |
| 2 | GET | `/api/venues/active` | `CONN-01`, `E2E-VEN-02` |
| 3 | GET | `/api/artists/{id}` | `E2E-ART-01`, `E2E-ART-01N` |
| 4 | GET | `/api/artists/by-stage-name` | `E2E-ART-02` |
| 5 | GET | `/api/artists/active` | `E2E-ART-03` |
| 6 | POST | `/api/users` | `E2E-USR-01/01B/01D/01V`, `ERR-02` |
| 7 | GET | `/api/users/by-email` | `E2E-USR-02/02X/02E/02A` |
| 8 | GET | `/api/users/by-username` | `E2E-USR-03/03X` |
| 9 | POST | `/api/events` | `E2E-EVT-01/01X/01U`, `SEC-07`, `ERR-01/05/06/08` |
| 10 | GET | `/api/events/{eventCode}` | `E2E-EVT-02/02D/02DS/02DA`, `FLU-C1` |
| 11 | GET | `/api/events/published` | `E2E-EVT-03`, `SEC-04`, `FLU-C2` |
| 12 | PATCH | `/api/events/{eventCode}/publish` | `E2E-EVT-04/04X/04R`, `ERR-07` |
| 13 | POST | `/api/events/{eventCode}/artists/{artistId}` | `E2E-EVT-05/05R/05X`, `SEC-08` |
| 14 | GET | `/api/events/by-artist` | `E2E-EVT-06`, `ERR-04`, `ERR-09` |
| 15 | POST | `/api/tickets` | `E2E-TKT-01/01T/01X/01D/01E/01A/01M` |
| 16 | GET | `/api/tickets/{ticketCode}` | `E2E-TKT-02/02X/02N`, `TKT-05V/06V` |
| 17 | GET | `/api/tickets/by-user` | `E2E-TKT-03/03X`, `FLU-D1` |
| 18 | GET | `/api/events/{eventCode}/tickets/paid` | `E2E-TKT-04/04X/04N`, `FLU-D2` |
| 19 | PATCH | `/api/tickets/{ticketCode}/cancel` | `E2E-TKT-05/05X/05R`, `SEC-09` |
| 20 | PATCH | `/api/tickets/{ticketCode}/use` | `E2E-TKT-06/06X/06R` |
| 21 | POST | `/api/auth/login` | `E2E-AUTH-01/01E/01B/01S/01A/01P/01X/01I`, `ERR-03` |
| 22 | GET | `/api/auth/me` | `E2E-AUTH-02/02S/02A/02N`, `SEC-01..06` |

**22/22 rutas.** Cada ruta tiene además casos negativos de rol, propiedad o
validación según su riesgo.

### Roles cubiertos

`ANONYMOUS`, `USER` (A y B), `STAFF`, `ADMIN`, `INACTIVE_USER` — más los
negativos de rol (USER en rutas ADMIN/OPS; STAFF en operación exclusiva de
ADMIN y en cancelación ajena).

---

## 6. Cómo comparar respuestas (revisión manual)

Para **cada** request inspeccionar en Postman: **Status**, **Headers**
(`Content-Type: application/json`) y **Body**:

1. **HTTP exacto:** `200`/`201` de éxito, o el código esperado del caso
   (`400/401/403/404/409`). Un `2xx` por sí solo **no** es aprobación.
2. **JSON:** debe ser el DTO real descrito en la descripción del caso
   (`VenueResponse`, `EventResponse`, `TicketResponse`, etc.). Nunca debe
   aparecer `password`, `passwordHash`, `secret` ni entidades JPA internas.
3. **`ErrorResponse`** en errores: exactamente 5 campos y `status` = HTTP:

   ```json
   { "timestamp": "...", "status": 403, "error": "Forbidden",
     "message": "...", "details": {} }
   ```

   Sin stack traces, hashes, tokens ni secretos de configuración. Mensajes
   reales de referencia: `Invalid credentials` (login 401),
   `Authentication is required to access this resource` (filtro 401),
   `No tiene permisos para realizar esta operacion` (403).
4. **Efecto de negocio:** tras cada transición, re-consultar con un GET
   posterior (`TKT-05V`, `TKT-06V`, `FLU-C1`, `FLU-D1`) y verificar el estado
   persistido. En mutaciones denegadas, re-leer con un actor autorizado y
   confirmar que **no** cambió.
5. Un `500` en cualquier escenario nominal es **FAIL** inmediato.

### Criterios PASS / FAIL / BLOCKED

- **PASS:** HTTP, JSON y condición de negocio/seguridad coinciden con lo esperado.
- **FAIL:** cualquier comprobación no coincide, o aparece un 500 inesperado.
- **BLOCKED:** no se puede ejecutar por falta de fixture, actor, token o
  respuesta previa indispensable. Los casos dependientes de un bloqueo **nunca**
  se marcan PASS.
- **NOT RUN:** aún no ejecutado (estado inicial de la bitácora).

---

## 7. Repetibilidad y limpieza

- **Reinicio completo (recomendado por sesión):**

  ```sql
  DROP DATABASE IF EXISTS pulsepass_e2e;
  CREATE DATABASE pulsepass_e2e;
  ```

  Rearrar la aplicación (Flyway recrea el esquema) y re-aplicar
  `fixtures/seed-e2e.sql`. Ver `fixtures/README.md`.
- **Reinicio ligero:** el seed es idempotente; re-ejecutarlo restaura eventos
  fixture, pero no borra tickets creados por HTTP (los estados
  CANCELLED/USED son irreversibles: usar códigos nuevos o reiniciar la BD).
- **Colisiones:** elegir un `runId` distinto por sesión y agregarlo a los
  correos/códigos nuevos (`userAEmail`, `newEventCode`, etc.).
- Nunca borrar ni reescribir registros de otras bases para repetir escenarios.

---

## 8. Confidencialidad

- No exportar la colección con valores en las variables de tipo *secret*
  (contraseñas y tokens); los JSON de este directorio se versionan **sin**
  valores sensibles (el `.gitignore` excluye `*.local.json`, `secrets/`,
  `exported/` y `.env`).
- En capturas y bitácora: **jamás** incluir JWT, contraseñas ni
  `PULSEPASS_JWT_SECRET_BASE64`.
- Los resultados se registran en `registro-ejecucion-manual.md`; al cerrar,
  consolidar PASS/FAIL/BLOCKED/NOT RUN por módulo y verificar 22/22 rutas con
  al menos un caso nominal ejecutado. No declarar aprobado lo no ejecutado.

---

## 9. Defectos y bloqueos

Al encontrar un FAIL, registrar: caso, precondiciones, pasos, HTTP
esperado/observado, JSON observado y cómo reproducirlo manualmente. Si las
pruebas revelan incompatibilidad con las Fases 1–4 (contratos, DTOs, roles),
anotarlo como **regresión** en la bitácora (FR-E2E-014). **No se modifica la
API** para acomodar la colección: la colección se ajusta al código real
(FR-E2E-017).
