package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PistaJpaRepository extends JpaRepository<PistaJpaEntity, UUID> {
    List<PistaJpaEntity> findByIdPartida(UUID idPartida);
}
