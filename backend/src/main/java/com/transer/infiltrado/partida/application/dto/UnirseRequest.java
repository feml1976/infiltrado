package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UnirseRequest(@NotBlank String codigoSala) {}
