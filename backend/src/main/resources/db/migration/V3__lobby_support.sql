-- =============================================================================
-- V3__lobby_support.sql
-- Soporte completo para la fase LOBBY del módulo partida:
--   1. partidas: añadir num_jugadores (cupo máximo), iniciada_at, finalizada_at
--   2. jugadores_partida: rol y codigo_4_digitos nullable (se asignan en iniciar()),
--      corregir CHECK de rol ('NORMAL' → 'INOCENTE'), añadir ha_senalado/ha_declarado
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Columnas de capacidad y auditoría temporal en partidas
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE partidas
    ADD COLUMN num_jugadores  INTEGER     NULL,
    ADD COLUMN iniciada_at    TIMESTAMPTZ NULL,
    ADD COLUMN finalizada_at  TIMESTAMPTZ NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- 2. jugadores_partida: rol y codigo_4_digitos son opcionales durante LOBBY
--    El dominio los asigna en iniciar(); hasta ese momento son NULL.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE jugadores_partida
    ALTER COLUMN rol              DROP NOT NULL,
    ALTER COLUMN codigo_4_digitos DROP NOT NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Corregir CHECK de rol: el dominio usa INOCENTE, no NORMAL
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE jugadores_partida
    DROP CONSTRAINT jp_rol_ck;

ALTER TABLE jugadores_partida
    ADD CONSTRAINT jp_rol_ck CHECK (rol IS NULL OR rol IN ('INOCENTE', 'INFILTRADO'));


-- ─────────────────────────────────────────────────────────────────────────────
-- 4. Banderas de fase y nombre de juego
--    nombre: denormalizado desde usuarios para evitar joins en lecturas de partida
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE jugadores_partida
    ADD COLUMN ha_senalado  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN ha_declarado BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN nombre       VARCHAR(100) NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- 5. Convertir CHAR → VARCHAR en partidas y jugadores_partida
--    Hibernate 6 (Spring Boot 3.x) mapea String a VARCHAR; CHAR (bpchar en PG)
--    causa errores de schema-validation con ddl-auto=validate.
--    CHAR(n) y VARCHAR(n) son semánticamente equivalentes para códigos de longitud fija.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE partidas
    ALTER COLUMN codigo_sala TYPE VARCHAR(6);

ALTER TABLE jugadores_partida
    ALTER COLUMN codigo_4_digitos TYPE VARCHAR(4);
