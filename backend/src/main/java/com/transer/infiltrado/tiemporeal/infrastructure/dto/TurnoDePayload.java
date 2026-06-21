package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Payload de turno_de. Sin rol ni cosa — el cliente no necesita esa info aquí. */
public record TurnoDePayload(UUID idJugador, String nombreJugador, int ordenTurno, int rondaActual) {}
