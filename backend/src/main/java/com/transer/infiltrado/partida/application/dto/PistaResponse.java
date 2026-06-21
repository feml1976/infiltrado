package com.transer.infiltrado.partida.application.dto;

import java.util.UUID;

public record PistaResponse(
        UUID   id,
        UUID   idJugador,
        String nombreJugador,
        int    ronda,
        int    ordenEnRonda,
        String contenido
) {}
