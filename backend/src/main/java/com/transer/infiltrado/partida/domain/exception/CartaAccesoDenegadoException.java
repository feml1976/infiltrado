package com.transer.infiltrado.partida.domain.exception;

public class CartaAccesoDenegadoException extends RuntimeException {
    public CartaAccesoDenegadoException() {
        super("No tienes acceso a esta carta");
    }
}
