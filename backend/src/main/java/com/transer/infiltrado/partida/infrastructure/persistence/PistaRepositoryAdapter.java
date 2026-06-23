package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.Pista;
import com.transer.infiltrado.partida.domain.PistaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
class PistaRepositoryAdapter implements PistaRepository {

    private final PistaJpaRepository jpaRepo;

    PistaRepositoryAdapter(PistaJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Pista guardar(Pista pista) {
        PistaJpaEntity entity = new PistaJpaEntity();
        entity.id           = pista.getId();
        entity.idPartida    = pista.getIdPartida();
        entity.idJugador    = pista.getIdJugador();
        entity.ronda        = pista.getRonda();
        entity.ordenEnRonda = pista.getOrdenEnRonda();
        entity.contenido    = pista.getContenido();
        entity.createdAt    = pista.getCreadaEn();
        jpaRepo.save(entity);
        return pista;
    }

    @Override
    public List<Pista> buscarPorPartida(UUID idPartida) {
        return jpaRepo.findByIdPartida(idPartida).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminarPorPartida(UUID idPartida) {
        jpaRepo.deleteByIdPartida(idPartida);
    }

    private Pista toDomain(PistaJpaEntity e) {
        return Pista.reconstituir(e.id, e.idPartida, e.idJugador,
                e.ronda, e.ordenEnRonda, e.contenido, e.createdAt);
    }
}
