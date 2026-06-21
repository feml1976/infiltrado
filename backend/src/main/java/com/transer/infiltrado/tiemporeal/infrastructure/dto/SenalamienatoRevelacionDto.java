package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Par señalador-señalado en el evento revelacion. */
public record SenalamienatoRevelacionDto(UUID idJugadorOrigen, UUID idJugadorSenalado) {}
