package com.transer.infiltrado.partida.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SenalamienatoJpaRepository extends JpaRepository<SenalamienatoJpaEntity, UUID> {
    List<SenalamienatoJpaEntity> findByIdPartida(UUID idPartida);
    void deleteByIdPartida(UUID idPartida);
}
