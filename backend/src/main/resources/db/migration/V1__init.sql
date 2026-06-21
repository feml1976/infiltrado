-- =============================================================================
-- V1__init.sql  Esquema base de El Infiltrado
-- =============================================================================
-- Convenciones:
--   • snake_case  •  tablas en plural  •  columnas en singular
--   •  UUID para PKs (gen_random_uuid() nativo desde PG 13)
--   •  soft deletes con deleted_at TIMESTAMP NULL
--   •  DATO SENSIBLE: codigo_4_digitos y codigo_sala → nunca a logs
-- =============================================================================

-- ── USUARIOS ──────────────────────────────────────────────────────────────────
CREATE TABLE usuarios (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    celular         VARCHAR(20)  NULL,
    nombre          VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    es_admin        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITHOUT TIME ZONE NULL,
    CONSTRAINT usuarios_email_uq UNIQUE (email)
);

CREATE INDEX idx_usuarios_email ON usuarios (email) WHERE deleted_at IS NULL;


-- ── COSAS (banco de palabras e imágenes) ──────────────────────────────────────
-- nombre: único, normalizado a singular/minúsculas en capa de aplicación
-- imagen_base64: obligatorio cuando tipo = 'IMAGEN'
CREATE TABLE cosas (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL,
    tipo            VARCHAR(10)  NOT NULL,
    imagen_base64   TEXT         NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITHOUT TIME ZONE NULL,
    CONSTRAINT cosas_nombre_uq          UNIQUE (nombre),
    CONSTRAINT cosas_tipo_ck            CHECK  (tipo IN ('PALABRA', 'IMAGEN')),
    CONSTRAINT cosas_imagen_required_ck CHECK  (
        tipo = 'PALABRA'
        OR (tipo = 'IMAGEN' AND imagen_base64 IS NOT NULL)
    )
);

CREATE INDEX idx_cosas_activo ON cosas (activo) WHERE deleted_at IS NULL;


-- ── PARTIDAS ──────────────────────────────────────────────────────────────────
-- codigo_sala: DATO SENSIBLE — 6 caracteres alfanuméricos en mayúsculas
-- estado sigue la máquina:
--   LOBBY → EN_CURSO → SENALAMIENTO → ADIVINANZA → REVELACION → FINALIZADA
CREATE TABLE partidas (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_sala     CHAR(6)      NOT NULL,
    id_moderador    UUID         NOT NULL,
    estado          VARCHAR(15)  NOT NULL DEFAULT 'LOBBY',
    num_infiltrados INTEGER      NOT NULL,
    num_rondas      INTEGER      NOT NULL,
    ronda_actual    INTEGER      NOT NULL DEFAULT 0,
    turno_actual    INTEGER      NOT NULL DEFAULT 0,
    id_cosa         UUID         NULL,
    modalidad       VARCHAR(10)  NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITHOUT TIME ZONE NULL,
    CONSTRAINT partidas_codigo_sala_uq      UNIQUE (codigo_sala),
    CONSTRAINT partidas_estado_ck           CHECK  (estado IN (
        'LOBBY','EN_CURSO','SENALAMIENTO','ADIVINANZA','REVELACION','FINALIZADA'
    )),
    CONSTRAINT partidas_num_rondas_ck       CHECK  (num_rondas BETWEEN 2 AND 5),
    CONSTRAINT partidas_num_infiltrados_ck  CHECK  (num_infiltrados >= 1),
    CONSTRAINT partidas_modalidad_ck        CHECK  (modalidad IN ('PALABRA', 'IMAGEN')),
    CONSTRAINT partidas_id_moderador_fk     FOREIGN KEY (id_moderador) REFERENCES usuarios (id),
    CONSTRAINT partidas_id_cosa_fk          FOREIGN KEY (id_cosa)      REFERENCES cosas    (id)
);

CREATE INDEX idx_partidas_estado ON partidas (estado) WHERE deleted_at IS NULL;


-- ── JUGADORES_PARTIDA ─────────────────────────────────────────────────────────
-- codigo_4_digitos: DATO SENSIBLE — nunca exponer en listados ni en logs
CREATE TABLE jugadores_partida (
    id               UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    id_partida       UUID        NOT NULL,
    id_usuario       UUID        NOT NULL,
    rol              VARCHAR(10) NOT NULL,
    codigo_4_digitos CHAR(4)     NOT NULL,
    orden_turno      INTEGER     NOT NULL,
    puntos_partida   INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT jp_partida_usuario_uq   UNIQUE  (id_partida, id_usuario),
    CONSTRAINT jp_partida_codigo_uq    UNIQUE  (id_partida, codigo_4_digitos),
    CONSTRAINT jp_partida_orden_uq     UNIQUE  (id_partida, orden_turno),
    CONSTRAINT jp_rol_ck               CHECK   (rol IN ('NORMAL', 'INFILTRADO')),
    CONSTRAINT jp_id_partida_fk        FOREIGN KEY (id_partida) REFERENCES partidas         (id),
    CONSTRAINT jp_id_usuario_fk        FOREIGN KEY (id_usuario) REFERENCES usuarios          (id)
);

CREATE INDEX idx_jp_partida ON jugadores_partida (id_partida);
CREATE INDEX idx_jp_usuario  ON jugadores_partida (id_usuario);


-- ── PISTAS ────────────────────────────────────────────────────────────────────
CREATE TABLE pistas (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    id_partida          UUID        NOT NULL,
    id_jugador          UUID        NOT NULL,
    ronda               INTEGER     NOT NULL,
    texto               VARCHAR(500) NOT NULL,
    marcada_sospechosa  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pistas_id_partida_fk FOREIGN KEY (id_partida) REFERENCES partidas          (id),
    CONSTRAINT pistas_id_jugador_fk FOREIGN KEY (id_jugador) REFERENCES jugadores_partida (id)
);

CREATE INDEX idx_pistas_partida_ronda ON pistas (id_partida, ronda);


-- ── REVISIONES (pistas sometidas a votación Sí/No) ───────────────────────────
CREATE TABLE revisiones (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    id_partida           UUID        NOT NULL,
    id_pista             UUID        NOT NULL,
    tipo                 VARCHAR(20) NOT NULL,
    id_jugador_proponente UUID       NOT NULL,
    resultado            VARCHAR(10) NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT rev_tipo_ck       CHECK  (tipo      IN ('NOMBRAR_COSA', 'PISTA_SOSPECHOSA')),
    CONSTRAINT rev_resultado_ck  CHECK  (resultado IN ('ROMPIO', 'NO_ROMPIO')),
    CONSTRAINT rev_id_partida_fk FOREIGN KEY (id_partida)            REFERENCES partidas          (id),
    CONSTRAINT rev_id_pista_fk   FOREIGN KEY (id_pista)              REFERENCES pistas            (id),
    CONSTRAINT rev_proponente_fk FOREIGN KEY (id_jugador_proponente) REFERENCES jugadores_partida (id)
);


-- ── VOTOS_REVISION ────────────────────────────────────────────────────────────
CREATE TABLE votos_revision (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    id_revision   UUID       NOT NULL,
    id_jugador    UUID       NOT NULL,
    voto          BOOLEAN    NOT NULL,
    CONSTRAINT vr_revision_jugador_uq UNIQUE  (id_revision, id_jugador),
    CONSTRAINT vr_id_revision_fk      FOREIGN KEY (id_revision) REFERENCES revisiones         (id),
    CONSTRAINT vr_id_jugador_fk       FOREIGN KEY (id_jugador)  REFERENCES jugadores_partida  (id)
);


-- ── SENALAMIENTOS (fase de cierre) ───────────────────────────────────────────
CREATE TABLE senalamientos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_partida          UUID NOT NULL,
    id_jugador_origen   UUID NOT NULL,
    id_jugador_senalado UUID NOT NULL,
    CONSTRAINT sen_uq         UNIQUE  (id_partida, id_jugador_origen, id_jugador_senalado),
    CONSTRAINT sen_partida_fk FOREIGN KEY (id_partida)          REFERENCES partidas          (id),
    CONSTRAINT sen_origen_fk  FOREIGN KEY (id_jugador_origen)   REFERENCES jugadores_partida (id),
    CONSTRAINT sen_senalado_fk FOREIGN KEY (id_jugador_senalado) REFERENCES jugadores_partida (id)
);


-- ── ADIVINANZAS (fase de adivinanza del infiltrado) ──────────────────────────
CREATE TABLE adivinanzas (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    id_partida           UUID         NOT NULL,
    id_jugador_infiltrado UUID        NOT NULL,
    texto_adivinanza     VARCHAR(500) NOT NULL,
    acierto              BOOLEAN      NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT adiv_id_partida_fk FOREIGN KEY (id_partida)            REFERENCES partidas          (id),
    CONSTRAINT adiv_jugador_fk    FOREIGN KEY (id_jugador_infiltrado)  REFERENCES jugadores_partida (id)
);


-- ── PUNTUACIONES_HISTORICAS (acumulado global) ───────────────────────────────
CREATE TABLE puntuaciones_historicas (
    id          UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario  UUID     NOT NULL,
    id_partida  UUID     NOT NULL,
    puntos      INTEGER  NOT NULL DEFAULT 0,
    fecha       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ph_id_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuarios  (id),
    CONSTRAINT ph_id_partida_fk FOREIGN KEY (id_partida) REFERENCES partidas  (id)
);

CREATE INDEX idx_ph_usuario ON puntuaciones_historicas (id_usuario);
CREATE INDEX idx_ph_partida ON puntuaciones_historicas (id_partida);
