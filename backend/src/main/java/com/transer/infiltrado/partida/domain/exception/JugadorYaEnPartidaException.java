package com.transer.infiltrado.partida.domain.exception;

import java.util.UUID;

public class JugadorYaEnPartidaException extends RuntimeException {
    public JugadorYaEnPartidaException(UUID idUsuario) {
        super("El usuario ya está en esta partida: " + idUsuario);
    }
}
