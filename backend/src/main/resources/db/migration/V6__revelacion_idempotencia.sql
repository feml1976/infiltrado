-- Garantía de idempotencia: un único registro por (usuario, partida)
-- Un juego continuado (CONTINUAR) actualiza el mismo registro vía upsert desde la aplicación.
ALTER TABLE puntuaciones_historicas
    ADD CONSTRAINT ux_ph_usuario_partida UNIQUE (id_usuario, id_partida);
