package com.transer.infiltrado.partida.domain.exception;

import java.util.UUID;

public class RevisionNoEncontradaException extends RuntimeException {
    public RevisionNoEncontradaException(UUID idRevision) {
        super("Revisión no encontrada: " + idRevision);
    }
}
