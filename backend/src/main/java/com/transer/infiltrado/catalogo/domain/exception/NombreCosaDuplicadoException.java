package com.transer.infiltrado.catalogo.domain.exception;

public class NombreCosaDuplicadoException extends RuntimeException {
    public NombreCosaDuplicadoException(String nombre) {
        super("Ya existe una cosa activa con el nombre: " + nombre);
    }
}
