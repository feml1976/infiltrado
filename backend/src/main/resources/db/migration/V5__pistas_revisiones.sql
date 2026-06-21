-- =============================================================================
-- V5__pistas_revisiones.sql
-- Reconciliación: pistas, revisiones y votos_revision ya fueron creadas en V1
-- con otro vocabulario. Esta migración las redefine con los nombres del dominio
-- del Paso 9 (contenido/estado/id_jugador_acusado/valor) y restaura las FKs e
-- índices que la versión intermedia había perdido.
--
-- Seguro de ejecutar: estas tablas NUNCA se poblaron (el Paso 9 no llegó a
-- correr), por lo que el DROP+CREATE no destruye datos.
--
-- ON DELETE sin CASCADE: coherente con la decisión del Paso 7 (NO ACTION +
-- limpieza manual en orden inverso de FK en los tests). Con soft deletes, el
-- borrado físico es raro e intencional.
-- =============================================================================

-- Orden de DROP: de la hija a la madre para respetar las FKs.
DROP TABLE IF EXISTS votos_revision CASCADE;
DROP TABLE IF EXISTS revisiones     CASCADE;
DROP TABLE IF EXISTS pistas         CASCADE;


-- ─────────────────────────────────────────────────────────────────────────────
-- pistas — una pista por jugador y por ronda (registrada durante EN_CURSO)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE pistas (
    id              UUID         PRIMARY KEY,
    id_partida      UUID         NOT NULL,
    id_jugador      UUID         NOT NULL,
    ronda           INT          NOT NULL,
    orden_en_ronda  INT          NOT NULL,
    contenido       VARCHAR(500) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pistas_id_partida_fk FOREIGN KEY (id_partida) REFERENCES partidas          (id),
    CONSTRAINT pistas_id_jugador_fk FOREIGN KEY (id_jugador) REFERENCES jugadores_partida (id),
    CONSTRAINT pistas_ronda_ck          CHECK (ronda >= 1),
    CONSTRAINT pistas_orden_en_ronda_ck CHECK (orden_en_ronda >= 1),
    -- Invariante de dominio: cada jugador aporta una sola pista por ronda.
    CONSTRAINT pistas_jugador_ronda_uq  UNIQUE (id_partida, id_jugador, ronda)
);

CREATE INDEX idx_pistas_partida ON pistas (id_partida);
CREATE INDEX idx_pistas_jugador ON pistas (id_jugador);


-- ─────────────────────────────────────────────────────────────────────────────
-- revisiones — NOMBRAR_COSA / PISTA_SOSPECHOSA sometidas a votación
-- Ventana de revisión: EN_CURSO, SENALAMIENTO, ADIVINANZA; cierra en REVELACION.
-- estado: ABIERTA → (ROMPIO | NO_ROMPIO). Empate en la votación = NO_ROMPIO.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE revisiones (
    id                  UUID        PRIMARY KEY,
    id_partida          UUID        NOT NULL,
    id_jugador_acusado  UUID        NOT NULL,
    tipo                VARCHAR(20) NOT NULL,
    estado              VARCHAR(12) NOT NULL DEFAULT 'ABIERTA',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT revisiones_id_partida_fk FOREIGN KEY (id_partida)         REFERENCES partidas          (id),
    CONSTRAINT revisiones_acusado_fk    FOREIGN KEY (id_jugador_acusado) REFERENCES jugadores_partida (id),
    CONSTRAINT revisiones_tipo_ck   CHECK (tipo   IN ('NOMBRAR_COSA', 'PISTA_SOSPECHOSA')),
    CONSTRAINT revisiones_estado_ck CHECK (estado IN ('ABIERTA', 'ROMPIO', 'NO_ROMPIO'))
);

CREATE INDEX idx_revisiones_partida ON revisiones (id_partida);
CREATE INDEX idx_revisiones_acusado ON revisiones (id_jugador_acusado);


-- ─────────────────────────────────────────────────────────────────────────────
-- votos_revision — un voto por jugador por revisión
-- valor: TRUE = "Sí rompió la regla". Sí > No → ROMPIO; Sí <= No → NO_ROMPIO.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE votos_revision (
    id          UUID        PRIMARY KEY,
    id_revision UUID        NOT NULL,
    id_jugador  UUID        NOT NULL,
    valor       BOOLEAN     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT votos_revision_id_revision_fk FOREIGN KEY (id_revision) REFERENCES revisiones         (id),
    CONSTRAINT votos_revision_id_jugador_fk  FOREIGN KEY (id_jugador)  REFERENCES jugadores_partida (id),
    CONSTRAINT ux_votos_revision UNIQUE (id_revision, id_jugador)
);

CREATE INDEX idx_votos_revision_revision ON votos_revision (id_revision);
CREATE INDEX idx_votos_revision_jugador  ON votos_revision (id_jugador);
