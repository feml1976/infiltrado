package com.transer.infiltrado.partida.application.port;

import com.transer.infiltrado.partida.domain.Adivinanza;
import com.transer.infiltrado.partida.domain.Senalamiento;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Datos completos para el evento revelacion WS.
 * Solo se emite cuando el estado es REVELACION.
 */
public record DatosRevelacion(
        List<JugadorRevelacionDto> jugadores,
        UUID idCosa,
        String nombreCosa,
        List<Senalamiento> senalamientos,
        List<Adivinanza> adivinanzas,
        Map<UUID, Integer> deltasPorJugador
) {}
