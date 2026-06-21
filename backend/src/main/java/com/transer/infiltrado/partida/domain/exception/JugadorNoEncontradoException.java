package com.transer.infiltrado.partida.domain.exception;

import java.util.UUID;

public class JugadorNoEncontradoException extends RuntimeException {
    public JugadorNoEncontradoException(UUID idJugador) {
        super("Jugador no encontrado en esta partida: " + idJugador);
    }
}
