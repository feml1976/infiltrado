package com.transer.infiltrado.partida.domain;

import java.time.Instant;
import java.util.UUID;

public record PuntuacionHistoricaEntry(UUID idPartida, int puntos, Instant fecha) {}
