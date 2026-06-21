package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface PartidaJpaRepository extends JpaRepository<PartidaJpaEntity, UUID> {
    Optional<PartidaJpaEntity> findByCodigoSala(String codigoSala);
    boolean existsByCodigoSala(String codigoSala);
}
