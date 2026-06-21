package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Emitido tras cada señalamiento. No incluye rol ni cosa. */
public record ProgresoSenalamienatoPayload(UUID idJugador, String nombre, int pendientes) {}
