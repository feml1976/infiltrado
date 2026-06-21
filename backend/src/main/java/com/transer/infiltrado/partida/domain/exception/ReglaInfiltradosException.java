package com.transer.infiltrado.partida.domain.exception;

public class ReglaInfiltradosException extends RuntimeException {
    public ReglaInfiltradosException(int numInfiltrados, int numJugadores, int maxPermitido) {
        super(String.format(
                "Número de infiltrados inválido: %d para %d jugadores (máximo permitido: %d, " +
                "regla: num_infiltrados <= floor((n-1)/2))",
                numInfiltrados, numJugadores, maxPermitido));
    }
}
