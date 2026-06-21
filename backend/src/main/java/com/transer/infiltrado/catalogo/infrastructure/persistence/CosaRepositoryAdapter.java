package com.transer.infiltrado.catalogo.infrastructure.persistence;

import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class CosaRepositoryAdapter implements CosaRepository {

    private final CosaJpaRepository jpa;

    CosaRepositoryAdapter(CosaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cosa guardar(Cosa cosa) {
        CosaJpaEntity e = cosa.getId() != null
                ? jpa.findById(cosa.getId()).orElseGet(CosaJpaEntity::new)
                : new CosaJpaEntity();
        e.setNombre(cosa.getNombre());
        e.setTipo(cosa.getTipo());
        e.setImagenBase64(cosa.getImagenBase64());
        e.setActivo(cosa.isActivo());
        return toDomain(jpa.save(e));
    }

    @Override
    public Optional<Cosa> buscarPorId(UUID id) {
        return jpa.findByIdAndDeletedAtIsNull(id).map(this::toDomain);
    }

    @Override
    public List<Cosa> listarActivos() {
        return jpa.findAllByDeletedAtIsNullOrderByNombre().stream()
                .map(p -> Cosa.reconstituir(p.getId(), p.getNombre(), p.getTipo(),
                        null, p.isActivo(), p.getCreatedAt()))
                .toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return jpa.existsByNombreActivo(nombre);
    }

    @Override
    public boolean existePorNombreExcluyendo(String nombre, UUID idExcluido) {
        return jpa.existsByNombreActivoExcluyendo(nombre, idExcluido);
    }

    @Override
    public void desactivar(UUID id, Instant eliminadoEn) {
        jpa.desactivar(id, eliminadoEn);
    }

    @Override
    public Optional<Cosa> seleccionarAleatoria() {
        return jpa.findOneRandom().map(this::toDomain);
    }

    private Cosa toDomain(CosaJpaEntity e) {
        return Cosa.reconstituir(e.getId(), e.getNombre(), e.getTipo(),
                e.getImagenBase64(), e.isActivo(), e.getCreatedAt());
    }
}
