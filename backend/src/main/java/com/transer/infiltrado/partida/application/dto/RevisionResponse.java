package com.transer.infiltrado.partida.application.dto;

import com.transer.infiltrado.partida.domain.EstadoRevision;
import com.transer.infiltrado.partida.domain.TipoRevision;

import java.util.List;
import java.util.UUID;

public record RevisionResponse(
        UUID            id,
        UUID            idPartida,
        UUID            idJugadorAcusado,
        TipoRevision    tipo,
        EstadoRevision  estado,
        List<VotoResumen> votos
) {}
