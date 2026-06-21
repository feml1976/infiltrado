package com.transer.infiltrado.partida.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Lista de jugadores a señalar. Puede ser vacía (abstención).
 */
public record RegistrarSenalamienatoRequest(@NotNull List<UUID> idsSenalados) {}
