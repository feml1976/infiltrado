package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface PuntuacionHistoricaJpaRepository extends JpaRepository<PuntuacionHistoricaJpaEntity, UUID> {

    Optional<PuntuacionHistoricaJpaEntity> findByIdUsuarioAndIdPartida(UUID idUsuario, UUID idPartida);
}
