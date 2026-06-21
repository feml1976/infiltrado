package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.EstadoRevision;
import com.transer.infiltrado.partida.domain.TipoRevision;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "revisiones")
class RevisionJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Transient
    private boolean esNueva = true;

    @Column(name = "id_partida", nullable = false)
    UUID idPartida;

    @Column(name = "id_jugador_acusado", nullable = false)
    UUID idJugadorAcusado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    TipoRevision tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    EstadoRevision estado;

    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<VotoJpaEntity> votos = new ArrayList<>();

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
