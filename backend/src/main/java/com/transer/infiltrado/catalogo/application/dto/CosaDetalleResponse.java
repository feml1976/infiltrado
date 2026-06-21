package com.transer.infiltrado.catalogo.application.dto;

import com.transer.infiltrado.catalogo.domain.TipoCosa;

import java.time.Instant;
import java.util.UUID;

public record CosaDetalleResponse(UUID id, String nombre, TipoCosa tipo, boolean activo,
                                   Instant creadoEn, String imagenBase64) {}
