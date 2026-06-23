package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.Senalamiento;
import com.transer.infiltrado.partida.domain.SenalamienatoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
class SenalamienatoRepositoryAdapter implements SenalamienatoRepository {

    private final SenalamienatoJpaRepository jpaRepo;

    SenalamienatoRepositoryAdapter(SenalamienatoJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Senalamiento guardar(Senalamiento s) {
        SenalamienatoJpaEntity entity = new SenalamienatoJpaEntity();
        entity.id                = s.getId();
        entity.idPartida         = s.getIdPartida();
        entity.idJugadorOrigen   = s.getIdJugadorOrigen();
        entity.idJugadorSenalado = s.getIdJugadorSenalado();
        entity.createdAt         = s.getCreadoEn();
        jpaRepo.save(entity);
        return s;
    }

    @Override
    public List<Senalamiento> buscarPorPartida(UUID idPartida) {
        return jpaRepo.findByIdPartida(idPartida).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminarPorPartida(UUID idPartida) {
        jpaRepo.deleteByIdPartida(idPartida);
    }

    private Senalamiento toDomain(SenalamienatoJpaEntity e) {
        return Senalamiento.reconstituir(
                e.id, e.idPartida, e.idJugadorOrigen, e.idJugadorSenalado, e.createdAt);
    }
}
