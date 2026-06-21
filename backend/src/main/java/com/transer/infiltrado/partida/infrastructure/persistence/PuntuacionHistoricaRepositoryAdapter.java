package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.PuntuacionHistoricaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
class PuntuacionHistoricaRepositoryAdapter implements PuntuacionHistoricaRepository {

    private final PuntuacionHistoricaJpaRepository jpaRepo;

    PuntuacionHistoricaRepositoryAdapter(PuntuacionHistoricaJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void upsertPuntos(UUID idUsuario, UUID idPartida, int puntos) {
        PuntuacionHistoricaJpaEntity entity = jpaRepo
                .findByIdUsuarioAndIdPartida(idUsuario, idPartida)
                .orElse(new PuntuacionHistoricaJpaEntity());

        if (entity.id == null) entity.id = UUID.randomUUID();
        entity.idUsuario = idUsuario;
        entity.idPartida = idPartida;
        entity.puntos    = puntos;
        entity.fecha     = Instant.now();

        jpaRepo.save(entity);
    }
}
