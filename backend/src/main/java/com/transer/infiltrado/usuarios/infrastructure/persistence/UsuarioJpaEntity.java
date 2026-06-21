package com.transer.infiltrado.usuarios.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String celular;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "es_admin", nullable = false)
    private boolean esAdmin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // ── Getters/Setters (package-private; solo el adaptador los usa) ──────────

    UUID getId()                 { return id; }
    String getEmail()            { return email; }
    void setEmail(String v)      { this.email = v; }
    String getNombre()           { return nombre; }
    void setNombre(String v)     { this.nombre = v; }
    String getCelular()          { return celular; }
    void setCelular(String v)    { this.celular = v; }
    String getPasswordHash()     { return passwordHash; }
    void setPasswordHash(String v) { this.passwordHash = v; }
    boolean isEsAdmin()          { return esAdmin; }
    void setEsAdmin(boolean v)   { this.esAdmin = v; }
    Instant getCreatedAt()       { return createdAt; }
    Instant getDeletedAt()       { return deletedAt; }
}
