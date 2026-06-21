package com.transer.infiltrado.partida.domain;

import java.time.Instant;
import java.util.UUID;

public final class Adivinanza {

    private final UUID id;
    private final UUID idPartida;
    private final UUID idJugadorInfiltrado;
    private final String textoAdivinanza;
    private final Boolean acierto; // null hasta que se evalúe en Paso 11
    private final Instant creadoEn;

    private Adivinanza(UUID id, UUID idPartida, UUID idJugadorInfiltrado,
                       String textoAdivinanza, Boolean acierto, Instant creadoEn) {
        this.id                  = id;
        this.idPartida           = idPartida;
        this.idJugadorInfiltrado = idJugadorInfiltrado;
        this.textoAdivinanza     = textoAdivinanza;
        this.acierto             = acierto;
        this.creadoEn            = creadoEn;
    }

    public static Adivinanza nueva(UUID idPartida, UUID idJugadorInfiltrado, String textoAdivinanza) {
        return new Adivinanza(UUID.randomUUID(), idPartida, idJugadorInfiltrado,
                textoAdivinanza, null, Instant.now());
    }

    public static Adivinanza reconstituir(UUID id, UUID idPartida, UUID idJugadorInfiltrado,
                                           String textoAdivinanza, Boolean acierto, Instant creadoEn) {
        return new Adivinanza(id, idPartida, idJugadorInfiltrado, textoAdivinanza, acierto, creadoEn);
    }

    public Adivinanza conAcierto(boolean acierto) {
        return new Adivinanza(id, idPartida, idJugadorInfiltrado,
                textoAdivinanza, acierto, creadoEn);
    }

    public UUID getId()                   { return id; }
    public UUID getIdPartida()            { return idPartida; }
    public UUID getIdJugadorInfiltrado()  { return idJugadorInfiltrado; }
    public String getTextoAdivinanza()    { return textoAdivinanza; }
    public Boolean getAcierto()           { return acierto; }
    public Instant getCreadoEn()          { return creadoEn; }
}
