package com.transer.infiltrado.partida.domain;

import java.time.Instant;
import java.util.UUID;

public final class Pista {

    private final UUID id;
    private final UUID idPartida;
    private final UUID idJugador;   // Jugador.getId() — UUID del dominio
    private final int  ronda;
    private final int  ordenEnRonda; // ordenTurno del jugador
    private final String contenido;
    private final Instant creadaEn;

    private Pista(UUID id, UUID idPartida, UUID idJugador,
                  int ronda, int ordenEnRonda, String contenido, Instant creadaEn) {
        this.id          = id;
        this.idPartida   = idPartida;
        this.idJugador   = idJugador;
        this.ronda       = ronda;
        this.ordenEnRonda = ordenEnRonda;
        this.contenido   = contenido;
        this.creadaEn    = creadaEn;
    }

    public static Pista nueva(UUID idPartida, UUID idJugador,
                               int ronda, int ordenEnRonda, String contenido) {
        return new Pista(UUID.randomUUID(), idPartida, idJugador,
                ronda, ordenEnRonda, contenido, Instant.now());
    }

    public static Pista reconstituir(UUID id, UUID idPartida, UUID idJugador,
                                      int ronda, int ordenEnRonda,
                                      String contenido, Instant creadaEn) {
        return new Pista(id, idPartida, idJugador, ronda, ordenEnRonda, contenido, creadaEn);
    }

    public UUID getId()           { return id; }
    public UUID getIdPartida()    { return idPartida; }
    public UUID getIdJugador()    { return idJugador; }
    public int  getRonda()        { return ronda; }
    public int  getOrdenEnRonda() { return ordenEnRonda; }
    public String getContenido()  { return contenido; }
    public Instant getCreadaEn()  { return creadaEn; }
}
