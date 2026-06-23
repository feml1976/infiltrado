package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.Revision;
import com.transer.infiltrado.partida.domain.RevisionRepository;
import com.transer.infiltrado.partida.domain.Voto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class RevisionRepositoryAdapter implements RevisionRepository {

    private final RevisionJpaRepository jpaRepo;

    RevisionRepositoryAdapter(RevisionJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Revision guardar(Revision revision) {
        RevisionJpaEntity entity = jpaRepo.findById(revision.getId())
                .orElse(new RevisionJpaEntity());

        entity.id               = revision.getId();
        entity.idPartida        = revision.getIdPartida();
        entity.idJugadorAcusado = revision.getIdJugadorAcusado();
        entity.tipo             = revision.getTipo();
        entity.estado           = revision.getEstado();
        entity.createdAt        = revision.getCreadaEn();

        syncVotos(revision, entity);

        jpaRepo.save(entity);
        return revision;
    }

    @Override
    public Optional<Revision> buscarPorId(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Revision> buscarPorPartida(UUID idPartida) {
        return jpaRepo.findByIdPartida(idPartida).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminarPorPartida(UUID idPartida) {
        // deleteAll con entidades cargadas garantiza el cascade a votos_revision
        jpaRepo.deleteAll(jpaRepo.findByIdPartida(idPartida));
    }

    private void syncVotos(Revision revision, RevisionJpaEntity entity) {
        Set<UUID> idsExistentes = entity.votos.stream()
                .map(v -> v.idJugador)
                .collect(Collectors.toSet());

        for (Voto voto : revision.getVotos()) {
            if (!idsExistentes.contains(voto.idJugador())) {
                VotoJpaEntity ve = new VotoJpaEntity(); // esNueva = true por defecto
                ve.id        = UUID.randomUUID();
                ve.revision  = entity;
                ve.idJugador = voto.idJugador();
                ve.valor     = voto.votoSi();
                entity.votos.add(ve);
            }
        }
    }

    private Revision toDomain(RevisionJpaEntity e) {
        List<Voto> votos = e.votos.stream()
                .map(v -> new Voto(v.idJugador, v.valor))
                .toList();
        return Revision.reconstituir(e.id, e.idPartida, e.idJugadorAcusado,
                e.tipo, e.estado, votos, e.createdAt);
    }
}
