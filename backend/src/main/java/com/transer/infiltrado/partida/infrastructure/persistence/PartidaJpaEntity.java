package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.EstadoPartida;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "partidas")
class PartidaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Column(name = "codigo_sala", nullable = false, length = 6)
    String codigoSala;

    @Column(name = "id_moderador", nullable = false)
    UUID idModerador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    EstadoPartida estado;

    @Column(name = "num_infiltrados", nullable = false)
    int numInfiltrados;

    @Column(name = "num_rondas", nullable = false)
    int numRondas;

    @Column(name = "num_jugadores")
    Integer numJugadores;

    @Column(name = "ronda_actual", nullable = false)
    int rondaActual;

    @Column(name = "turno_actual", nullable = false)
    int turnoActual;

    @Column(name = "id_cosa")
    UUID idCosa;

    @Column(name = "modalidad", length = 10)
    String modalidad;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Column(name = "iniciada_at")
    Instant iniciadaAt;

    @Column(name = "finalizada_at")
    Instant finalizadaAt;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<JugadorPartidaJpaEntity> jugadores = new ArrayList<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
