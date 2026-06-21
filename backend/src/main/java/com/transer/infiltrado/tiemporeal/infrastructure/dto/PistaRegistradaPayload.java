package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Payload de pista_registrada. Identifica quién dio la pista, sin revelar el contenido a otros. */
public record PistaRegistradaPayload(UUID idJugador, String nombreJugador) {}
