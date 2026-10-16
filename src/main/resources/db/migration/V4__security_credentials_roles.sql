-- Fase 4 - Capa de Seguridad
-- Credenciales y roles. Migracion incremental: no altera ni reescribe V1-V3.

-- Credenciales (1:1 con users). La PK es tambien FK hacia users.
CREATE TABLE user_credentials (
                                 user_id             BIGINT       NOT NULL,
                                 password_hash       VARCHAR(100) NOT NULL,
                                 password_changed_at TIMESTAMP,
                                 CONSTRAINT pk_user_credentials PRIMARY KEY (user_id),
                                 CONSTRAINT fk_user_credentials_user
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Roles (N:1 con users). PK compuesta (user_id, role).
CREATE TABLE user_roles (
                            user_id BIGINT      NOT NULL,
                            role    VARCHAR(20) NOT NULL,
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT chk_user_roles_role CHECK (role IN ('USER', 'STAFF', 'ADMIN'))
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);

-- Los usuarios legados sin credenciales quedan sin posibilidad de login.
-- No se asignan contrasenas por defecto ni compartidas.
