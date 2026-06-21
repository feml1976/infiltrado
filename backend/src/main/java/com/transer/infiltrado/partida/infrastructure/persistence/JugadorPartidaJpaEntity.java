package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.RolJugador;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jugadores_partida")
class JugadorPartidaJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    /** Indica si esta instancia es nueva (nunca persistida). Gestionado por hooks JPA. */
    @Transient
    private boolean esNueva = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    PartidaJpaEntity partida;

    @Column(name = "id_usuario", nullable = false)
    UUID idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 10)
    RolJugador rol;

    // DATO SENSIBLE — nunca incluir en logs
    @Column(name = "codigo_4_digitos", length = 4)
    String codigo4Digitos;

    @Column(name = "orden_turno", nullable = false)
    int ordenTurno;

    @Column(name = "puntos_partida", nullable = false)
    int puntosPartida;

    @Column(name = "ha_senalado", nullable = false)
    boolean haSenalado;

    @Column(name = "ha_declarado", nullable = false)
    boolean haDeclarado;

    @Column(name = "nombre")
    String nombre;

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
