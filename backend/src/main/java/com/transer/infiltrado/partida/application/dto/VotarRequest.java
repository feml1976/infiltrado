package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.NotNull;

public record VotarRequest(@NotNull Boolean votoSi) {}
