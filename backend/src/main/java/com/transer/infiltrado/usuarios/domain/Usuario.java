package com.transer.infiltrado.usuarios.domain;

import java.time.Instant;
import java.util.UUID;

public final class Usuario {

    private final UUID id;
    private final String email;
    private final String nombre;
    private final String celular;
    private final String passwordHash;
    private final boolean esAdmin;
    private final Instant createdAt;

    private Usuario(UUID id, String email, String nombre, String celular,
                    String passwordHash, boolean esAdmin, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.celular = celular;
        this.passwordHash = passwordHash;
        this.esAdmin = esAdmin;
        this.createdAt = createdAt;
    }

    /** Crea un nuevo usuario (alta self-service). esAdmin siempre false. */
    public static Usuario nuevo(String email, String nombre, String celular, String passwordHash) {
        return new Usuario(null, email, nombre, celular, passwordHash, false, null);
    }

    /** Reconstituye un usuario desde persistencia. */
    public static Usuario reconstituir(UUID id, String email, String nombre, String celular,
                                       String passwordHash, boolean esAdmin, Instant createdAt) {
        return new Usuario(id, email, nombre, celular, passwordHash, esAdmin, createdAt);
    }

    public UUID getId()           { return id; }
    public String getEmail()      { return email; }
    public String getNombre()     { return nombre; }
    public String getCelular()    { return celular; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEsAdmin()    { return esAdmin; }
    public Instant getCreatedAt() { return createdAt; }
}
