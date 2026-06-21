package com.transer.infiltrado.partida.application.port;

import com.transer.infiltrado.partida.domain.RolJugador;

import java.util.UUID;

public record JugadorRolDto(UUID idJugador, String nombre, RolJugador rol) {}
