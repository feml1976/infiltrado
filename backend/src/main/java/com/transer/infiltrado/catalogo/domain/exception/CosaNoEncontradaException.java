package com.transer.infiltrado.catalogo.domain.exception;

public class CosaNoEncontradaException extends RuntimeException {
    public CosaNoEncontradaException() {
        super("Cosa no encontrada");
    }
}
