package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarAdivinanzaRequest(
        @NotBlank @Size(max = 500) String textoAdivinanza
) {}
