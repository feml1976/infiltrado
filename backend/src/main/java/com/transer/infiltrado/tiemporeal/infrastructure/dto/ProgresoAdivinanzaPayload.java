package com.transer.infiltrado.tiemporeal.infrastructure.dto;

/**
 * Emitido tras cada declaración de adivinanza.
 * Solo expone el conteo de infiltrados que aún no declararon — nunca el nombre ni el rol.
 */
public record ProgresoAdivinanzaPayload(int pendientes) {}
