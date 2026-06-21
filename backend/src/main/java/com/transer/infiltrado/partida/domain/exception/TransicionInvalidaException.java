package com.transer.infiltrado.partida.domain.exception;

public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
