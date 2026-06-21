package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.Adivinanza;
import com.transer.infiltrado.partida.domain.AdivinanzaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
class AdivinanzaRepositoryAdapter implements AdivinanzaRepository {

    private final AdivinanzaJpaRepository jpaRepo;

    AdivinanzaRepositoryAdapter(AdivinanzaJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Adivinanza guardar(Adivinanza a) {
        // findById permite que @PostLoad marque esNueva=false → UPDATE en lugar de INSERT duplicado
        AdivinanzaJpaEntity entity = jpaRepo.findById(a.getId())
                .orElse(new AdivinanzaJpaEntity());
        entity.id                  = a.getId();
        entity.idPartida           = a.getIdPartida();
        entity.idJugadorInfiltrado = a.getIdJugadorInfiltrado();
        entity.textoAdivinanza     = a.getTextoAdivinanza();
        entity.acierto             = a.getAcierto();
        entity.createdAt           = a.getCreadoEn(); // updatable=false, inofensivo en UPDATE
        jpaRepo.save(entity);
        return a;
    }

    @Override
    public List<Adivinanza> buscarPorPartida(UUID idPartida) {
        return jpaRepo.findByIdPartida(idPartida).stream()
                .map(this::toDomain)
                .toList();
    }

    private Adivinanza toDomain(AdivinanzaJpaEntity e) {
        return Adivinanza.reconstituir(
                e.id, e.idPartida, e.idJugadorInfiltrado,
                e.textoAdivinanza, e.acierto, e.createdAt);
    }
}
