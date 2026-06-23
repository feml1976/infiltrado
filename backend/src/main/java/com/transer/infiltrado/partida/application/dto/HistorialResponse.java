package com.transer.infiltrado.partida.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HistorialResponse(List<Item> partidas) {

    public record Item(UUID idPartida, int puntos, Instant fecha) {}
}
