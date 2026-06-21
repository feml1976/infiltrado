package com.transer.infiltrado.partida.application.dto;

import com.transer.infiltrado.partida.domain.RolJugador;

import java.util.UUID;

/**
 * Carta personal del jugador — exclusiva del propietario autenticado.
 * Campos nulos mientras la partida sigue en LOBBY (aún no se ha inicializado).
 */
public record CartaResponse(
        RolJugador rol,
        UUID idCosa,
        String nombreCosa,
        String tipo,
        String imagenBase64
) {}
