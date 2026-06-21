package com.transer.infiltrado.partida.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votos_revision",
       uniqueConstraints = @UniqueConstraint(name = "ux_votos_revision",
               columnNames = {"id_revision", "id_jugador"}))
class VotoJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Transient
    private boolean esNueva = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revision", nullable = false)
    RevisionJpaEntity revision;

    @Column(name = "id_jugador", nullable = false)
    UUID idJugador;

    @Column(name = "valor", nullable = false)
    boolean valor;

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
