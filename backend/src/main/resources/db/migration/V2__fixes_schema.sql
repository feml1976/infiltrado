-- =============================================================================
-- V2__fixes_schema.sql
-- Correcciones de schema identificadas en revisión post-V1:
--   • TIMESTAMP → TIMESTAMPTZ en todas las columnas de fecha
--   • Índices únicos parciales (case-insensitive, solo registros activos)
--   • Índices sobre FK faltantes
--   • CHECK de auto-señalamiento en senalamientos
--   • CHECK de columnas nullable más explícitos
--   • created_at en jugadores_partida, senalamientos, votos_revision
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- 1. TIMESTAMPTZ — corregir todas las columnas de fecha
--    Rationale: TIMESTAMP WITHOUT TIME ZONE es ambiguo ante cambios de zona
--    horaria del servidor. TIMESTAMPTZ almacena en UTC y convierte en lectura.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE usuarios
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMPTZ USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE cosas
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMPTZ USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE partidas
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMPTZ USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE pistas
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE revisiones
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE adivinanzas
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE puntuaciones_historicas
    ALTER COLUMN fecha TYPE TIMESTAMPTZ USING fecha AT TIME ZONE 'UTC';


-- ─────────────────────────────────────────────────────────────────────────────
-- 2. Unicidad parcial y case-insensitive
--    Reemplaza las restricciones UNIQUE duras por índices únicos parciales que:
--      (a) solo aplican sobre registros activos (deleted_at IS NULL)
--      (b) normalizan a minúsculas para evitar colisiones de capitalización
-- ─────────────────────────────────────────────────────────────────────────────

-- usuarios.email
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_email_uq;
CREATE UNIQUE INDEX ux_usuarios_email_activo ON usuarios (lower(email)) WHERE deleted_at IS NULL;

-- cosas.nombre
-- La capa de aplicación normaliza a minúsculas/singular antes de persistir;
-- el índice es defensa en profundidad.
ALTER TABLE cosas DROP CONSTRAINT IF EXISTS cosas_nombre_uq;
CREATE UNIQUE INDEX ux_cosas_nombre_activo ON cosas (lower(nombre)) WHERE deleted_at IS NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Índices sobre FK faltantes
--    PostgreSQL no indexa FKs automáticamente; sin índice, JOINs y borrados
--    hacen seq scan incluso en tablas pequeñas.
-- ─────────────────────────────────────────────────────────────────────────────

-- pistas
CREATE INDEX idx_pistas_jugador   ON pistas     (id_jugador);

-- revisiones
CREATE INDEX idx_rev_partida      ON revisiones (id_partida);
CREATE INDEX idx_rev_pista        ON revisiones (id_pista);
CREATE INDEX idx_rev_proponente   ON revisiones (id_jugador_proponente);

-- votos_revision (id_revision ya cubierto por el UNIQUE; falta id_jugador)
CREATE INDEX idx_vr_jugador       ON votos_revision (id_jugador);

-- senalamientos (el UNIQUE (id_partida, origen, senalado) cubre scans por
-- id_partida; se agregan índices independientes para origen y señalado)
CREATE INDEX idx_sen_origen       ON senalamientos (id_jugador_origen);
CREATE INDEX idx_sen_senalado     ON senalamientos (id_jugador_senalado);

-- adivinanzas
CREATE INDEX idx_adiv_partida     ON adivinanzas (id_partida);
CREATE INDEX idx_adiv_jugador     ON adivinanzas (id_jugador_infiltrado);


-- ─────────────────────────────────────────────────────────────────────────────
-- 4. CHECK de auto-señalamiento en senalamientos
--    Ningún jugador puede señalarse a sí mismo.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE senalamientos
    ADD CONSTRAINT sen_no_auto_senalamiento_ck
        CHECK (id_jugador_origen <> id_jugador_senalado);


-- ─────────────────────────────────────────────────────────────────────────────
-- 5. CHECK de columnas nullable más explícitos
--    El comportamiento es idéntico (NULL supera cualquier CHECK), pero la
--    forma explícita comunica la intención al lector del schema.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE partidas
    DROP CONSTRAINT IF EXISTS partidas_modalidad_ck,
    ADD  CONSTRAINT partidas_modalidad_ck
        CHECK (modalidad IS NULL OR modalidad IN ('PALABRA', 'IMAGEN'));

ALTER TABLE revisiones
    DROP CONSTRAINT IF EXISTS rev_resultado_ck,
    ADD  CONSTRAINT rev_resultado_ck
        CHECK (resultado IS NULL OR resultado IN ('ROMPIO', 'NO_ROMPIO'));


-- ─────────────────────────────────────────────────────────────────────────────
-- 6. Añadir created_at a tablas sin auditoría de timestamp
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE jugadores_partida
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE senalamientos
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE votos_revision
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
