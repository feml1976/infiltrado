package com.transer.infiltrado.partida.domain.exception;

public class PartidaNoEncontradaException extends RuntimeException {
    public PartidaNoEncontradaException() {
        super("Partida no encontrada");
    }
    public PartidaNoEncontradaException(String codigoOId) {
        super("Partida no encontrada");
    }
}
