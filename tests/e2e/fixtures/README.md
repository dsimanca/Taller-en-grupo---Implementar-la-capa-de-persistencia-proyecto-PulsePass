# Fixtures E2E — PulsePass (Fase 5)

Datos de prueba **exclusivos** para la ejecución manual de la colección Postman.
Nunca ejecutar estos scripts sobre bases de desarrollo, QA ni producción.

## Archivos

| Archivo | Propósito |
|---|---|
| `seed-e2e.sql` | Venue activo, usuarios privilegiados (`e2e_admin`, `e2e_staff`), cuenta inactiva y eventos fixture (DRAFT, PUBLISHED, por publicar, +18). Idempotente (`ON CONFLICT DO NOTHING`). |

## Credenciales de fixtures (solo pruebas)

| Actor | Username | Email | Contraseña | Rol |
|---|---|---|---|---|
| `ADMIN` | `e2e_admin` | `e2e.admin@example.test` | `E2e-Passw0rd-2026!` | `ADMIN` |
| `STAFF` | `e2e_staff` | `e2e.staff@example.test` | `E2e-Passw0rd-2026!` | `STAFF` |
| `INACTIVE_USER` | `e2e_inactive` | `e2e.inactive@example.test` | `E2e-Passw0rd-2026!` | `USER` (cuenta `active = FALSE`) |

- Estas credenciales **no son secretos reales**: son cuentas de prueba locales.
  Cambiarlas si el repositorio se publica fuera del contexto académico.
- El `password_hash` es BCrypt (`$2a$10$...`) generado y verificado con
  `BCryptPasswordEncoder` del proyecto.
- `STAFF` y `ADMIN` **no** se pueden crear por HTTP: el registro público
  (`POST /api/users`) solo asigna `USER` y no acepta roles en el cuerpo.
- `USER_A` y `USER_B` **no** se siembran aquí: se registran por HTTP en el
  Flujo B de la colección (cada corrida usa sus propios correos con `{{runId}}`).

## Cómo aplicar el seed

1. Crear (si no existe) la base E2E dedicada:

   ```sql
   CREATE DATABASE pulsepass_e2e;
   ```

2. Levantar la aplicación apuntando a esa base (variables `PULSEPASS_DB_URL`,
   `PULSEPASS_DB_USERNAME`, `PULSEPASS_DB_PASSWORD`) para que Flyway aplique
   V1..V4; **o** aplicar migraciones antes con la CLI de Flyway.

3. Aplicar el seed una vez migrado:

   ```bash
   psql "$PULSEPASS_DB_URL" -U "$PULSEPASS_DB_USERNAME" -f tests/e2e/fixtures/seed-e2e.sql
   ```

## Fixtures creados y su uso en la colección

| Fixture | Valor | Uso |
|---|---|---|
| Venue activo | `E2E-VENUE-01` | `{{venueActiveCode}}` — crear eventos y verificar `GET /api/venues/{code}`. |
| Artista activo | `Solar Beat` (Flyway V2) | `{{artistStageName}}`; `{{artistId}}` se obtiene de `GET /api/artists/by-stage-name`. |
| Evento borrador | `E2E-EVT-DRAFT` | `{{draftEventCode}}` — visibilidad 404 para anónimo/USER, 200 para OPS. |
| Evento publicado | `E2E-EVT-PUBLISHED` | `{{publishedEventCode}}` — compra de tickets y flujos públicos. |
| Evento por publicar | `E2E-EVT-PUBLISH` | `{{eventToPublishCode}}` — `PATCH /publish`. Consumir con `runId` nuevo o re-siembrar. |
| Evento +18 | `E2E-EVT-AGE18` | `{{ageRestrictedEventCode}}` — compra rechazada por edad mínima (409). |
| Tickets | *(no sembrados)* | `{{ticketCancelableCode}}` y `{{ticketUsableCode}}` se obtienen comprando por HTTP. |

## Limpieza y repetibilidad

- **Reinicio completo (recomendado por sesión):**

  ```sql
  DROP DATABASE IF EXISTS pulsepass_e2e;
  CREATE DATABASE pulsepass_e2e;
  ```

  Volver a levantar la app (Flyway recrea el esquema) y re-aplicar `seed-e2e.sql`.

- **Reinicio ligero:** re-ejecutar `seed-e2e.sql` es seguro (idempotente) y
  restaura los eventos a su estado fixture, pero **no** borra tickets creados
  por HTTP; si un escenario quedó en estado irreversible (CANCELLED/USED),
  usar un código de ticket nuevo o reiniciar la BD completa.

- Nunca borrar registros de otras bases para repetir escenarios.
