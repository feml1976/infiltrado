package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.UUID;

/** Adivinanza de un infiltrado con resultado evaluado. */
public record AdivinanzaRevelacionDto(UUID idJugadorInfiltrado, String textoAdivinanza, boolean acierto) {}
