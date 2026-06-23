package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface PuntuacionHistoricaJpaRepository extends JpaRepository<PuntuacionHistoricaJpaEntity, UUID> {

    Optional<PuntuacionHistoricaJpaEntity> findByIdUsuarioAndIdPartida(UUID idUsuario, UUID idPartida);

    @Query("SELECT COALESCE(SUM(p.puntos), 0) FROM PuntuacionHistoricaJpaEntity p WHERE p.idUsuario = :idUsuario")
    long sumPuntosByIdUsuario(@Param("idUsuario") UUID idUsuario);

    List<PuntuacionHistoricaJpaEntity> findByIdUsuarioOrderByFechaDesc(UUID idUsuario);
}
