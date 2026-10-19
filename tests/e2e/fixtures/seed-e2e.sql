-- ============================================================================
-- PulsePass â€” Seed exclusivo para pruebas E2E manuales (Fase 5)
-- ============================================================================
-- Ambiente: BASE DE DATOS DE PRUEBA DEDICADA (pulsepass_e2e). NUNCA ejecutar
-- sobre bases de desarrollo, QA ni producciÃ³n.
--
-- Precondiciones:
--   1. Migraciones Flyway (V1..V4) ya aplicadas sobre la BD E2E.
--   2. Los 5 artistas sembrados por V2__insert_initial_artists.sql existen.
--
-- Idempotencia: todas las sentencias usan ON CONFLICT DO NOTHING; el script
-- puede re-ejecutarse sin duplicar datos.
--
-- Credenciales de fixtures (SOLO pruebas, cambiar en cualquier otro contexto):
--   e2e_admin   / E2e-Passw0rd-2026!
--   e2e_staff   / E2e-Passw0rd-2026!
--   e2e_inactive/ E2e-Passw0rd-2026!  (cuenta desactivada: active = FALSE)
-- El hash BCrypt fue generado y verificado con BCryptPasswordEncoder del
-- proyecto (spring-security-crypto). Los usuarios NO se crean por HTTP:
-- el registro pÃºblico solo produce rol USER.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Venue activo fixture (la Fase 3 no expone POST /api/venues)
-- ----------------------------------------------------------------------------
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('E2E-VENUE-01', 'PulsePass Arena E2E', 'Bogota', 'Calle E2E 123', 200, TRUE)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 2. Usuarios privilegiados y cuenta inactiva (fuera del registro pÃºblico)
-- ----------------------------------------------------------------------------
INSERT INTO users (username, email, active)
VALUES ('e2e_admin',    'e2e.admin@example.test',    TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, email, active)
VALUES ('e2e_staff',    'e2e.staff@example.test',    TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, email, active)
VALUES ('e2e_inactive', 'e2e.inactive@example.test', FALSE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_profiles (user_id, first_name, last_name, phone, city, birth_date)
SELECT u.id, 'Admin', 'E2E', '+57-300-000-0001', 'Bogota', DATE '1990-01-01'
FROM users u WHERE u.username = 'e2e_admin'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_profiles (user_id, first_name, last_name, phone, city, birth_date)
SELECT u.id, 'Staff', 'E2E', '+57-300-000-0002', 'Bogota', DATE '1992-02-02'
FROM users u WHERE u.username = 'e2e_staff'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_profiles (user_id, first_name, last_name, phone, city, birth_date)
SELECT u.id, 'Inactivo', 'E2E', '+57-300-000-0003', 'Bogota', DATE '1994-03-03'
FROM users u WHERE u.username = 'e2e_inactive'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at)
SELECT u.id, '$2a$10$kYqhcWOZWcyZvVZ/hhmDDuTSq/NRbIrwLJhvSJ1SbZPwBA4mtSZ2u', CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'e2e_admin'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at)
SELECT u.id, '$2a$10$kYqhcWOZWcyZvVZ/hhmDDuTSq/NRbIrwLJhvSJ1SbZPwBA4mtSZ2u', CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'e2e_staff'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at)
SELECT u.id, '$2a$10$kYqhcWOZWcyZvVZ/hhmDDuTSq/NRbIrwLJhvSJ1SbZPwBA4mtSZ2u', CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'e2e_inactive'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN' FROM users u WHERE u.username = 'e2e_admin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'STAFF' FROM users u WHERE u.username = 'e2e_staff'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'USER' FROM users u WHERE u.username = 'e2e_inactive'
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 3. Eventos fixture (fechas futuras lejanas para que no venzan en pocos dias)
-- ----------------------------------------------------------------------------
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id, streaming_url)
SELECT 'E2E-EVT-DRAFT', 'Evento borrador E2E', 'Fixture en DRAFT: invisible para publico',
       'MUSIC', 'DRAFT', TIMESTAMP '2027-06-15 20:00:00', 0, v.id, NULL
FROM venues v WHERE v.code = 'E2E-VENUE-01'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id, streaming_url)
SELECT 'E2E-EVT-PUBLISHED', 'Evento publicado E2E', 'Fixture PUBLISHED con aforo disponible',
       'MUSIC', 'PUBLISHED', TIMESTAMP '2027-07-20 18:00:00', 0, v.id, NULL
FROM venues v WHERE v.code = 'E2E-VENUE-01'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id, streaming_url)
SELECT 'E2E-EVT-PUBLISH', 'Evento por publicar E2E', 'Fixture DRAFT listo para PATCH /publish',
       'ENTERTAINMENT', 'DRAFT', TIMESTAMP '2027-08-10 19:00:00', 0, v.id, NULL
FROM venues v WHERE v.code = 'E2E-VENUE-01'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id, streaming_url)
SELECT 'E2E-EVT-AGE18', 'Evento +18 E2E', 'Fixture PUBLISHED con edad minima 18',
       'CULTURE', 'PUBLISHED', TIMESTAMP '2027-09-05 17:00:00', 18, v.id, NULL
FROM venues v WHERE v.code = 'E2E-VENUE-01'
ON CONFLICT (event_code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. Asociaciones evento <-> artista (artista 'Solar Beat' de Flyway V2)
-- ----------------------------------------------------------------------------
INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name = 'Solar Beat'
WHERE e.event_code = 'E2E-EVT-PUBLISHED'
ON CONFLICT DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name = 'Solar Beat'
WHERE e.event_code = 'E2E-EVT-DRAFT'
ON CONFLICT DO NOTHING;

-- PulsePass â€” Seed exclusivo para pruebas E2E manuales (Fase 5)

-- ----------------------------------------------------------------------------
-- 5. Venue y evento de capacidad 1 para pruebas de aforo (SOLD_OUT)
-- ----------------------------------------------------------------------------
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('E2E-VENUE-CAP1', 'PulsePass Arena CAP1 E2E', 'Bogota', 'Calle CAP1 E2E 124', 1, TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id, streaming_url)
SELECT 'E2E-EVT-CAP1', 'Evento aforo 1 E2E', 'Fixture PUBLISHED con capacidad 1 para agotar aforo',
       'MUSIC', 'PUBLISHED', TIMESTAMP '2027-07-25 18:00:00', 0, v.id, NULL
FROM venues v WHERE v.code = 'E2E-VENUE-CAP1'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name = 'Solar Beat'
WHERE e.event_code = 'E2E-EVT-CAP1'
ON CONFLICT DO NOTHING;
-- Fin del seed. Los tickets NO se siembran: se crean por HTTP en los flujos
-- D (compra -> cancelacion / uso) para probar las transiciones reales.
-- ============================================================================
