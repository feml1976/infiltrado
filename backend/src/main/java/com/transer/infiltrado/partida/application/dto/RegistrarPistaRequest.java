package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarPistaRequest(
        @NotBlank @Size(max = 500) String contenido
) {}
