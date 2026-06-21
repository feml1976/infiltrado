package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface AdivinanzaJpaRepository extends JpaRepository<AdivinanzaJpaEntity, UUID> {
    List<AdivinanzaJpaEntity> findByIdPartida(UUID idPartida);
}
