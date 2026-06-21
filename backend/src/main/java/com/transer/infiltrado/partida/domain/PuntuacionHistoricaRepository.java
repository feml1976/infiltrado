package com.transer.infiltrado.partida.domain;

import java.util.UUID;

public interface PuntuacionHistoricaRepository {

    /** Inserta o actualiza el total de puntos de un jugador en una partida (idempotente). */
    void upsertPuntos(UUID idUsuario, UUID idPartida, int puntos);
}
