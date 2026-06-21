package com.transer.infiltrado.partida.application.dto;

import java.util.List;
import java.util.UUID;

/** Respuesta completa del endpoint GET /{codigoSala}/revelacion. */
public record RevelacionResponse(
        UUID idPartida,
        String codigoSala,
        String nombreCosa,
        String tipoCosa,
        List<JugadorItem> jugadores,
        List<SenalamienatoItem> senalamientos,
        List<AdivinanzaItem> adivinanzas
) {
    public record JugadorItem(UUID id, String nombre, int ordenTurno,
                               String rol, int deltaRonda, int puntosAcumulados) {}

    public record SenalamienatoItem(UUID idJugadorOrigen, UUID idJugadorSenalado) {}

    public record AdivinanzaItem(UUID idJugadorInfiltrado, String textoAdivinanza, boolean acierto) {}
}
