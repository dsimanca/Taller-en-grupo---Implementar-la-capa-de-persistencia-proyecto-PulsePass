-- Tabla venues
CREATE TABLE venues (
                        id              BIGSERIAL PRIMARY KEY,
                        code            VARCHAR(50)  NOT NULL UNIQUE,
                        name            VARCHAR(150) NOT NULL,
                        city            VARCHAR(100) NOT NULL,
                        address         VARCHAR(255),
                        capacity        INTEGER      NOT NULL CHECK (capacity > 0),
                        active          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Tabla events
CREATE TABLE events (
                        id              BIGSERIAL PRIMARY KEY,
                        event_code      VARCHAR(50)  NOT NULL UNIQUE,
                        name            VARCHAR(200) NOT NULL,
                        description     TEXT,
                        category        VARCHAR(30)  NOT NULL,
                        status          VARCHAR(30)  NOT NULL,
                        event_date      TIMESTAMP    NOT NULL,
                        minimum_age     INTEGER      NOT NULL DEFAULT 0,
                        venue_id        BIGINT       NOT NULL,
                        CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
                        CONSTRAINT chk_event_category CHECK (category IN ('MUSIC','SPORTS','TECHNOLOGY','EDUCATION','CULTURE','ENTERTAINMENT')),
                        CONSTRAINT chk_event_status   CHECK (status   IN ('DRAFT','PUBLISHED','SOLD_OUT','CANCELLED','FINISHED'))
);
CREATE INDEX idx_events_status_date ON events(status, event_date);

-- Tabla artists
CREATE TABLE artists (
                         id              BIGSERIAL PRIMARY KEY,
                         stage_name      VARCHAR(150) NOT NULL UNIQUE,
                         country         VARCHAR(100),
                         genre           VARCHAR(100),
                         active          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Tabla N:M event_artists (PK compuesta)
CREATE TABLE event_artists (
                               event_id        BIGINT NOT NULL,
                               artist_id       BIGINT NOT NULL,
                               CONSTRAINT pk_event_artists PRIMARY KEY (event_id, artist_id),
                               CONSTRAINT fk_ea_event  FOREIGN KEY (event_id)  REFERENCES events(id)  ON DELETE CASCADE,
                               CONSTRAINT fk_ea_artist FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- Tabla users
CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       username        VARCHAR(80)  NOT NULL UNIQUE,
                       email           VARCHAR(150) NOT NULL UNIQUE,
                       active          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Tabla user_profiles (1:1 con users)
CREATE TABLE user_profiles (
                               id              BIGSERIAL PRIMARY KEY,
                               user_id         BIGINT       NOT NULL UNIQUE,
                               first_name      VARCHAR(100) NOT NULL,
                               last_name       VARCHAR(100) NOT NULL,
                               phone           VARCHAR(30),
                               city            VARCHAR(100),
                               birth_date      DATE,
                               CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla tickets
CREATE TABLE tickets (
                         id              BIGSERIAL PRIMARY KEY,
                         ticket_code     VARCHAR(50)  NOT NULL UNIQUE,
                         type            VARCHAR(20)  NOT NULL,
                         price           NUMERIC(12,2) NOT NULL CHECK (price >= 0),
                         status          VARCHAR(20)  NOT NULL,
                         purchase_date   TIMESTAMP    NOT NULL,
                         user_id         BIGINT       NOT NULL,
                         event_id        BIGINT       NOT NULL,
                         CONSTRAINT fk_ticket_user  FOREIGN KEY (user_id)  REFERENCES users(id),
                         CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES events(id),
                         CONSTRAINT chk_ticket_type   CHECK (type   IN ('GENERAL','VIP','BACKSTAGE','STUDENT')),
                         CONSTRAINT chk_ticket_status CHECK (status IN ('RESERVED','PAID','CANCELLED','USED'))
);
CREATE INDEX idx_tickets_event_status ON tickets(event_id, status);
CREATE INDEX idx_tickets_user ON tickets(user_id);