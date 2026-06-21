package com.transer.infiltrado.partida.application.port;

/**
 * Puerto de aplicación para generar un código de sala único de 6 caracteres alfanuméricos
 * en mayúsculas. La implementación usa SecureRandom y reintenta ante colisión de índice único.
 */
public interface GeneradorCodigoSala {
    String generar();
}
