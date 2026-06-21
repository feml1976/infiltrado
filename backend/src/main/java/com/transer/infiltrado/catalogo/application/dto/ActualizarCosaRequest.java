package com.transer.infiltrado.catalogo.application.dto;

import com.transer.infiltrado.catalogo.domain.TipoCosa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarCosaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull TipoCosa tipo,
        String imagenBase64
) {}
