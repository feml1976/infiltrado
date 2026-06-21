package com.transer.infiltrado.usuarios.domain.exception;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() {
        super("El email ya está registrado");
    }
}
