package com.transer.infiltrado.catalogo.infrastructure.persistence;

import com.transer.infiltrado.catalogo.domain.TipoCosa;

import java.time.Instant;
import java.util.UUID;

interface CosaResumenProjection {
    UUID getId();
    String getNombre();
    TipoCosa getTipo();
    boolean isActivo();
    Instant getCreatedAt();
}
