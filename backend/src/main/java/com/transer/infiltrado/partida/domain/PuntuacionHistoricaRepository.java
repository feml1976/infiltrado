package com.transer.infiltrado.partida.domain;

import java.util.List;
import java.util.UUID;

public interface PuntuacionHistoricaRepository {

    /** Inserta o actualiza el total de puntos de un jugador en una partida (idempotente). */
    void upsertPuntos(UUID idUsuario, UUID idPartida, int puntos);

    /** Suma de puntos de todas las partidas terminadas del usuario. */
    int acumuladoGlobal(UUID idUsuario);

    /** Lista de registros históricos del usuario, ordenados por fecha descendente. */
    List<PuntuacionHistoricaEntry> buscarHistorialPorUsuario(UUID idUsuario);
}
