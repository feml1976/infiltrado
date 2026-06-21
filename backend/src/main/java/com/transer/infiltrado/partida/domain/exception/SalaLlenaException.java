package com.transer.infiltrado.partida.domain.exception;

public class SalaLlenaException extends RuntimeException {
    public SalaLlenaException(int capacidad) {
        super("La sala ya alcanzó su capacidad máxima de " + capacidad + " jugadores");
    }
}
