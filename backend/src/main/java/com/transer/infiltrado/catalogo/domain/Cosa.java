package com.transer.infiltrado.catalogo.domain;

import java.time.Instant;
import java.util.UUID;

public final class Cosa {

    private final UUID id;
    private final String nombre;
    private final TipoCosa tipo;
    private final String imagenBase64; // null para PALABRA o en listados
    private final boolean activo;
    private final Instant creadoEn;

    private Cosa(UUID id, String nombre, TipoCosa tipo, String imagenBase64,
                 boolean activo, Instant creadoEn) {
        this.id           = id;
        this.nombre       = nombre;
        this.tipo         = tipo;
        this.imagenBase64 = imagenBase64;
        this.activo       = activo;
        this.creadoEn     = creadoEn;
    }

    public static Cosa nueva(String nombre, TipoCosa tipo, String imagenBase64) {
        return new Cosa(null, nombre, tipo, imagenBase64, true, null);
    }

    public static Cosa reconstituir(UUID id, String nombre, TipoCosa tipo,
                                    String imagenBase64, boolean activo, Instant creadoEn) {
        return new Cosa(id, nombre, tipo, imagenBase64, activo, creadoEn);
    }

    public UUID getId()             { return id; }
    public String getNombre()       { return nombre; }
    public TipoCosa getTipo()       { return tipo; }
    public String getImagenBase64() { return imagenBase64; }
    public boolean isActivo()       { return activo; }
    public Instant getCreadoEn()    { return creadoEn; }
}
