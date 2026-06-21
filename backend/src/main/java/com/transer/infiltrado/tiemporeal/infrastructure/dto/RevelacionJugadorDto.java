package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Jugador con rol revelado y desglose de puntos — solo para el evento revelacion. */
public record RevelacionJugadorDto(UUID idJugador, String nombreJugador, String rol,
                                    int deltaRonda, int puntosAcumulados) {}
