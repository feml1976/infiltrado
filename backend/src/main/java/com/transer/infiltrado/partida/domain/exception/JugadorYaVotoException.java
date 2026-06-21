package com.transer.infiltrado.partida.domain.exception;

import java.util.UUID;

public class JugadorYaVotoException extends RuntimeException {
    public JugadorYaVotoException(UUID idJugador) {
        super("El jugador " + idJugador + " ya emitió su voto en esta revisión");
    }
}
