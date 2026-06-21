package com.transer.infiltrado.partida.application.dto;

import com.transer.infiltrado.partida.domain.TipoRevision;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProponerRevisionRequest(
        @NotNull TipoRevision tipo,
        @NotNull UUID         idJugadorAcusado
) {}
