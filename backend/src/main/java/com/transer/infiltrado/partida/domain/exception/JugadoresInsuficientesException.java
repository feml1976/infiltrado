package com.transer.infiltrado.partida.domain.exception;

public class JugadoresInsuficientesException extends RuntimeException {
    public JugadoresInsuficientesException(int actual) {
        super("Se necesitan al menos 3 jugadores para iniciar. Jugadores actuales: " + actual);
    }
}
