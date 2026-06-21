package com.transer.infiltrado.catalogo.infrastructure.persistence;

import com.transer.infiltrado.catalogo.domain.TipoCosa;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cosas")
class CosaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCosa tipo;

    @Column(name = "imagen_base64", columnDefinition = "TEXT")
    private String imagenBase64;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    UUID     getId()             { return id; }
    String   getNombre()         { return nombre; }
    TipoCosa getTipo()           { return tipo; }
    String   getImagenBase64()   { return imagenBase64; }
    boolean  isActivo()          { return activo; }
    Instant  getCreatedAt()      { return createdAt; }
    Instant  getDeletedAt()      { return deletedAt; }

    void setNombre(String nombre)              { this.nombre = nombre; }
    void setTipo(TipoCosa tipo)                { this.tipo = tipo; }
    void setImagenBase64(String imagenBase64)  { this.imagenBase64 = imagenBase64; }
    void setActivo(boolean activo)             { this.activo = activo; }
    void setDeletedAt(Instant deletedAt)       { this.deletedAt = deletedAt; }
}
