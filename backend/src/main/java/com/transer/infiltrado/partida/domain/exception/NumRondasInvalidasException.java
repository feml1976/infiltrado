package com.transer.infiltrado.partida.domain.exception;

public class NumRondasInvalidasException extends RuntimeException {
    public NumRondasInvalidasException(int numRondas) {
        super("Número de rondas inválido: " + numRondas + ". Debe estar entre 2 y 5.");
    }
}
