package com.transer.infiltrado.partida.application.dto;

import com.transer.infiltrado.partida.domain.EstadoPartida;

import java.util.List;
import java.util.UUID;

/**
 * Estado público de la partida.
 * Nunca incluye el rol de ningún jugador antes de REVELACION — eso queda en /mi-carta.
 */
public record EstadoPartidaResponse(
        UUID id,
        String codigoSala,
        UUID idModerador,
        EstadoPartida estado,
        int numRondas,
        int numInfiltrados,
        int numJugadores,
        int rondaActual,
        List<JugadorResumenResponse> jugadores
) {}
