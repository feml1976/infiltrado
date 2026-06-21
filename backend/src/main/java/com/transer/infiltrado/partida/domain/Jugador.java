package com.transer.infiltrado.partida.domain;

import java.util.UUID;

/**
 * Entidad interna del agregado Partida.
 * Las mutaciones son package-private: solo Partida puede modificar el estado de un Jugador.
 */
public final class Jugador {

    private final UUID id;
    private final UUID idUsuario;
    private final String nombre;
    private final int ordenTurno; // 1-indexed, estable durante toda la partida

    private RolJugador rol;
    private String codigo4Digitos; // asignado en Paso 7
    private int puntosAcumulados;
    private boolean haSenalado;
    private boolean haDeclarado; // relevante solo para INFILTRADO en fase ADIVINANZA

    private Jugador(UUID id, UUID idUsuario, String nombre, int ordenTurno) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.ordenTurno = ordenTurno;
    }

    /** Crea un nuevo jugador con UUID generado en el dominio. */
    static Jugador nuevo(UUID idUsuario, String nombre, int ordenTurno) {
        return new Jugador(UUID.randomUUID(), idUsuario, nombre, ordenTurno);
    }

    public static Jugador reconstituir(UUID id, UUID idUsuario, String nombre, int ordenTurno,
                                 RolJugador rol, String codigo4Digitos,
                                 int puntosAcumulados, boolean haSenalado, boolean haDeclarado) {
        Jugador j = new Jugador(id, idUsuario, nombre, ordenTurno);
        j.rol              = rol;
        j.codigo4Digitos   = codigo4Digitos;
        j.puntosAcumulados = puntosAcumulados;
        j.haSenalado       = haSenalado;
        j.haDeclarado      = haDeclarado;
        return j;
    }

    // ── Mutaciones (package-private — accesibles solo desde Partida) ──────────

    void asignarRol(RolJugador rol)     { this.rol = rol; }
    void asignarCodigo(String codigo)   { this.codigo4Digitos = codigo; }
    void registrarSenalamiento()        { this.haSenalado = true; }
    void registrarDeclaracion()         { this.haDeclarado = true; }
    void sumarPuntos(int delta)         { this.puntosAcumulados += delta; }

    /**
     * Reinicia el estado de fase. Conserva puntosAcumulados y codigo4Digitos —
     * el código es fijo para toda la sesión y no se reasigna en CONTINUAR.
     */
    void resetearFase() {
        this.rol        = null;
        this.haSenalado = false;
        this.haDeclarado = false;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public UUID getId()                { return id; }
    public UUID getIdUsuario()         { return idUsuario; }
    public String getNombre()          { return nombre; }
    public int getOrdenTurno()         { return ordenTurno; }
    public RolJugador getRol()         { return rol; }
    public String getCodigo4Digitos()  { return codigo4Digitos; }
    public int getPuntosAcumulados()   { return puntosAcumulados; }
    public boolean isHaSenalado()      { return haSenalado; }
    public boolean isHaDeclarado()     { return haDeclarado; }
}
