package com.transer.infiltrado.partida.application.port;

import com.transer.infiltrado.partida.domain.RolJugador;

import java.util.UUID;

/** Jugador con rol revelado, incluye desglose de puntos de la ronda. */
public record JugadorRevelacionDto(
        UUID idJugador,
        String nombre,
        RolJugador rol,
        int deltaRonda,
        int puntosAcumulados
) {}
