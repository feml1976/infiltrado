package com.transer.infiltrado.partida.domain;

import java.time.Instant;
import java.util.UUID;

public final class Senalamiento {

    private final UUID id;
    private final UUID idPartida;
    private final UUID idJugadorOrigen;
    private final UUID idJugadorSenalado;
    private final Instant creadoEn;

    private Senalamiento(UUID id, UUID idPartida, UUID idJugadorOrigen,
                         UUID idJugadorSenalado, Instant creadoEn) {
        this.id                = id;
        this.idPartida         = idPartida;
        this.idJugadorOrigen   = idJugadorOrigen;
        this.idJugadorSenalado = idJugadorSenalado;
        this.creadoEn          = creadoEn;
    }

    public static Senalamiento nuevo(UUID idPartida, UUID idJugadorOrigen, UUID idJugadorSenalado) {
        return new Senalamiento(UUID.randomUUID(), idPartida, idJugadorOrigen,
                idJugadorSenalado, Instant.now());
    }

    public static Senalamiento reconstituir(UUID id, UUID idPartida, UUID idJugadorOrigen,
                                             UUID idJugadorSenalado, Instant creadoEn) {
        return new Senalamiento(id, idPartida, idJugadorOrigen, idJugadorSenalado, creadoEn);
    }

    public UUID getId()                { return id; }
    public UUID getIdPartida()         { return idPartida; }
    public UUID getIdJugadorOrigen()   { return idJugadorOrigen; }
    public UUID getIdJugadorSenalado() { return idJugadorSenalado; }
    public Instant getCreadoEn()       { return creadoEn; }
}
