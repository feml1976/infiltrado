package com.transer.infiltrado.usuarios.domain.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
