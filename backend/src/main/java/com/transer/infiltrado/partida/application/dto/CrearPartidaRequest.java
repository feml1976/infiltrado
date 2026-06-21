package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CrearPartidaRequest(
        @Min(2) @Max(5) int numRondas,
        @Min(1) int numInfiltrados,
        @Min(3) @Max(20) int numJugadores
) {}
