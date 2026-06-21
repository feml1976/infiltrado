package com.transer.infiltrado.tiemporeal.infrastructure.dto;

import java.util.List;
import java.util.UUID;

/** Payload de revelacion — único evento donde se exponen roles, cosa objetivo, aciertos y puntos. */
public record RevelacionPayload(
        List<RevelacionJugadorDto> jugadores,
        UUID idCosa,
        String nombreCosa,
        List<SenalamienatoRevelacionDto> senalamientos,
        List<AdivinanzaRevelacionDto> adivinanzas
) {}
