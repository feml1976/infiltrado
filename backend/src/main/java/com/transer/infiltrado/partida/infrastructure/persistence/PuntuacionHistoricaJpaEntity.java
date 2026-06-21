package com.transer.infiltrado.partida.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "puntuaciones_historicas")
class PuntuacionHistoricaJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Transient
    private boolean esNueva = true;

    @Column(name = "id_usuario", nullable = false)
    UUID idUsuario;

    @Column(name = "id_partida", nullable = false)
    UUID idPartida;

    @Column(name = "puntos", nullable = false)
    int puntos;

    @Column(name = "fecha", nullable = false)
    Instant fecha;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return esNueva; }

    @PostLoad
    @PostPersist
    void marcarComoExistente() { esNueva = false; }

    @PrePersist
    void prePersist() {
        if (fecha == null) fecha = Instant.now();
    }
}
