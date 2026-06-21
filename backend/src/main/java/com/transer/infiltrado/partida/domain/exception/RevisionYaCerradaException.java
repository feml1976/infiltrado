package com.transer.infiltrado.partida.domain.exception;

import java.util.UUID;

public class RevisionYaCerradaException extends RuntimeException {
    public RevisionYaCerradaException(UUID idRevision) {
        super("La revisión " + idRevision + " ya está cerrada");
    }
}
