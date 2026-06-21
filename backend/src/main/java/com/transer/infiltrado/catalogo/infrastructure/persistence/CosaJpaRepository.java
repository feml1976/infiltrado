package com.transer.infiltrado.catalogo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CosaJpaRepository extends JpaRepository<CosaJpaEntity, UUID> {

    Optional<CosaJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    /** Proyección sin imagen_base64 — Spring Data excluye la columna automáticamente. */
    List<CosaResumenProjection> findAllByDeletedAtIsNullOrderByNombre();

    @Query("SELECT COUNT(c) > 0 FROM CosaJpaEntity c WHERE lower(c.nombre) = lower(:nombre) AND c.deletedAt IS NULL")
    boolean existsByNombreActivo(@Param("nombre") String nombre);

    @Query("SELECT COUNT(c) > 0 FROM CosaJpaEntity c WHERE lower(c.nombre) = lower(:nombre) AND c.deletedAt IS NULL AND c.id <> :id")
    boolean existsByNombreActivoExcluyendo(@Param("nombre") String nombre, @Param("id") UUID id);

    @Modifying
    @Query("UPDATE CosaJpaEntity c SET c.deletedAt = :ts, c.activo = false WHERE c.id = :id")
    void desactivar(@Param("id") UUID id, @Param("ts") Instant ts);

    @Query(value = "SELECT * FROM cosas WHERE activo = true AND deleted_at IS NULL ORDER BY RANDOM() LIMIT 1",
           nativeQuery = true)
    Optional<CosaJpaEntity> findOneRandom();
}
