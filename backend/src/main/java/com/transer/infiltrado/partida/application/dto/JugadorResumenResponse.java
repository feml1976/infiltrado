package com.transer.infiltrado.partida.application.dto;

import java.util.UUID;

/** Vista pública de un jugador — nunca incluye rol ni codigo_4_digitos. */
public record JugadorResumenResponse(
        UUID id,
        String nombre,
        int ordenTurno,
        int puntosAcumulados,
        boolean haSenalado
) {}
