package com.transer.infiltrado.partida.domain;

import java.util.Set;

/**
 * Puerto de dominio para generar un código de 4 dígitos único dentro de una partida.
 * La implementación usa SecureRandom y reintenta ante colisión.
 */
public interface GeneradorCodigo4Digitos {
    /** Genera un código que no esté en {@code codigosExistentes}. */
    String generar(Set<String> codigosExistentes);
}
