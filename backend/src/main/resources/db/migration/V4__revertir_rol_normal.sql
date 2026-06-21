-- =============================================================================
-- V4__revertir_rol_normal.sql
-- Correcciones post-V3 acordadas con el equipo:
--   1. Revertir nombre del rol: INOCENTE → NORMAL (alineado con reglamento y CLAUDE.md)
--   2. num_jugadores NOT NULL (el cupo se fija al crear; nunca debe ser NULL)
-- El código de 4 dígitos persiste entre continuaciones (decisión UX por sesión).
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Revertir CHECK de rol: INOCENTE → NORMAL
--    V3 lo cambió de NORMAL a INOCENTE para seguir al enum del dominio.
--    El reglamento y la convención del proyecto usan NORMAL; se revierte.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE jugadores_partida
    DROP CONSTRAINT jp_rol_ck;

ALTER TABLE jugadores_partida
    ADD CONSTRAINT jp_rol_ck CHECK (rol IS NULL OR rol IN ('NORMAL', 'INFILTRADO'));


-- ─────────────────────────────────────────────────────────────────────────────
-- 2. num_jugadores NOT NULL — el cupo siempre se fija en la creación
-- ─────────────────────────────────────────────────────────────────────────────

UPDATE partidas SET num_jugadores = 5 WHERE num_jugadores IS NULL;

ALTER TABLE partidas
    ALTER COLUMN num_jugadores SET NOT NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Documentar semántica del nombre en jugadores_partida (comentario schema)
--    nombre es un snapshot del nombre del usuario al unirse; no se re-sincroniza
--    si el usuario cambia su nombre posterior al ingreso a la sala.
-- ─────────────────────────────────────────────────────────────────────────────

COMMENT ON COLUMN jugadores_partida.nombre IS
    'Snapshot del nombre del usuario al unirse. No se re-sincroniza si el usuario actualiza su nombre.';
