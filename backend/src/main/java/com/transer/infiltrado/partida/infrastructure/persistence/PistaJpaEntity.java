package com.transer.infiltrado.partida.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pistas")
class PistaJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Transient
    private boolean esNueva = true;

    @Column(name = "id_partida", nullable = false)
    UUID idPartida;

    @Column(name = "id_jugador", nullable = false)
    UUID idJugador;

    @Column(name = "ronda", nullable = false)
    int ronda;

    @Column(name = "orden_en_ronda", nullable = false)
    int ordenEnRonda;

    @Column(name = "contenido", nullable = false, length = 500)
    String contenido;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return esNueva; }

    @PostLoad
    @PostPersist
    void marcarComoExistente() { esNueva = false; }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
