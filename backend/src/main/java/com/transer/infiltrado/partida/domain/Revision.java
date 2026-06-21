package com.transer.infiltrado.partida.domain;

import com.transer.infiltrado.partida.domain.exception.JugadorYaVotoException;
import com.transer.infiltrado.partida.domain.exception.RevisionYaCerradaException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Revision {

    private final UUID id;
    private final UUID idPartida;
    private final UUID idJugadorAcusado;
    private final TipoRevision tipo;
    private EstadoRevision estado;
    private final List<Voto> votos;
    private final Instant creadaEn;

    private Revision(UUID id, UUID idPartida, UUID idJugadorAcusado,
                     TipoRevision tipo, EstadoRevision estado,
                     List<Voto> votos, Instant creadaEn) {
        this.id               = id;
        this.idPartida        = idPartida;
        this.idJugadorAcusado = idJugadorAcusado;
        this.tipo             = tipo;
        this.estado           = estado;
        this.votos            = votos;
        this.creadaEn         = creadaEn;
    }

    public static Revision nueva(UUID idPartida, UUID idJugadorAcusado, TipoRevision tipo) {
        return new Revision(UUID.randomUUID(), idPartida, idJugadorAcusado,
                tipo, EstadoRevision.ABIERTA, new ArrayList<>(), Instant.now());
    }

    public static Revision reconstituir(UUID id, UUID idPartida, UUID idJugadorAcusado,
                                         TipoRevision tipo, EstadoRevision estado,
                                         List<Voto> votos, Instant creadaEn) {
        return new Revision(id, idPartida, idJugadorAcusado,
                tipo, estado, new ArrayList<>(votos), creadaEn);
    }

    /**
     * Sí > No → ROMPIO; Sí ≤ No (empate incluido) → NO_ROMPIO (beneficio de la duda).
     * El cierre es automático cuando todos los jugadores han votado.
     */
    public void registrarVoto(UUID idJugador, boolean votoSi, int totalJugadores) {
        if (estado != EstadoRevision.ABIERTA) {
            throw new RevisionYaCerradaException(id);
        }
        if (votos.stream().anyMatch(v -> v.idJugador().equals(idJugador))) {
            throw new JugadorYaVotoException(idJugador);
        }
        votos.add(new Voto(idJugador, votoSi));
        if (votos.size() == totalJugadores) {
            cerrar();
        }
    }

    private void cerrar() {
        long si = votos.stream().filter(Voto::votoSi).count();
        long no = votos.size() - si;
        estado = (si > no) ? EstadoRevision.ROMPIO : EstadoRevision.NO_ROMPIO;
    }

    public boolean estaCerrada() { return estado != EstadoRevision.ABIERTA; }

    public UUID           getId()               { return id; }
    public UUID           getIdPartida()        { return idPartida; }
    public UUID           getIdJugadorAcusado() { return idJugadorAcusado; }
    public TipoRevision   getTipo()             { return tipo; }
    public EstadoRevision getEstado()           { return estado; }
    public List<Voto>     getVotos()            { return Collections.unmodifiableList(votos); }
    public Instant        getCreadaEn()         { return creadaEn; }
}
