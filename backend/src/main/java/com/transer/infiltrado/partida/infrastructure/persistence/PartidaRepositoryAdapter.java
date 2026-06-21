package com.transer.infiltrado.partida.infrastructure.persistence;

import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class PartidaRepositoryAdapter implements PartidaRepository {

    private final PartidaJpaRepository jpaRepository;

    PartidaRepositoryAdapter(PartidaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Partida guardar(Partida partida) {
        PartidaJpaEntity entity = partida.getId() != null
                ? jpaRepository.findById(partida.getId())
                        .orElseThrow(() -> new PartidaNoEncontradaException(partida.getId().toString()))
                : new PartidaJpaEntity();

        mapToEntity(partida, entity);
        PartidaJpaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Partida> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Partida> buscarPorCodigo(String codigoSala) {
        return jpaRepository.findByCodigoSala(codigoSala).map(this::mapToDomain);
    }

    @Override
    public boolean existePorCodigo(String codigoSala) {
        return jpaRepository.existsByCodigoSala(codigoSala);
    }

    // ── Mapeo entity → domain ──────────────────────────────────────────────────

    private Partida mapToDomain(PartidaJpaEntity e) {
        List<Jugador> jugadores = e.jugadores.stream()
                .sorted((a, b) -> Integer.compare(a.ordenTurno, b.ordenTurno))
                .map(this::mapJugadorToDomain)
                .toList();

        return Partida.reconstituir(
                e.id, e.idModerador, e.codigoSala,
                e.numRondas, e.numInfiltrados,
                e.numJugadores != null ? e.numJugadores : 0,
                e.estado, jugadores,
                e.rondaActual, e.turnoActual,
                e.idCosa, e.modalidad,
                e.createdAt, e.iniciadaAt, e.finalizadaAt);
    }

    private Jugador mapJugadorToDomain(JugadorPartidaJpaEntity je) {
        return Jugador.reconstituir(
                je.id, je.idUsuario,
                je.nombre != null ? je.nombre : "",
                je.ordenTurno,
                je.rol, je.codigo4Digitos,
                je.puntosPartida, je.haSenalado, je.haDeclarado);
    }

    // ── Mapeo domain → entity ──────────────────────────────────────────────────

    private void mapToEntity(Partida p, PartidaJpaEntity e) {
        e.codigoSala    = p.getCodigoSala();
        e.idModerador   = p.getIdModerador();
        e.estado        = p.getEstado();
        e.numInfiltrados = p.getNumInfiltrados();
        e.numRondas     = p.getNumRondasTotal();
        e.numJugadores  = p.getNumJugadores();
        e.rondaActual   = p.getRondaActual();
        e.turnoActual   = p.getIndiceTurnoActual();
        e.idCosa        = p.getIdCosaActual();
        e.modalidad     = p.getModalidad();
        e.iniciadaAt    = p.getIniciadaEn();
        e.finalizadaAt  = p.getFinalizadaEn();

        // Sincronizar jugadores: añadir nuevos, actualizar existentes
        List<UUID> idsEnDominio = p.getJugadores().stream().map(Jugador::getId).toList();
        e.jugadores.removeIf(je -> je.id != null && !idsEnDominio.contains(je.id));

        for (Jugador j : p.getJugadores()) {
            JugadorPartidaJpaEntity je = e.jugadores.stream()
                    .filter(x -> j.getId() != null && j.getId().equals(x.id))
                    .findFirst()
                    .orElseGet(() -> {
                        JugadorPartidaJpaEntity nuevo = new JugadorPartidaJpaEntity();
                        nuevo.partida = e;
                        e.jugadores.add(nuevo);
                        return nuevo;
                    });
            je.id            = j.getId();
            je.idUsuario     = j.getIdUsuario();
            je.nombre        = j.getNombre();
            je.ordenTurno    = j.getOrdenTurno();
            je.rol           = j.getRol();
            je.codigo4Digitos = j.getCodigo4Digitos();
            je.puntosPartida = j.getPuntosAcumulados();
            je.haSenalado    = j.isHaSenalado();
            je.haDeclarado   = j.isHaDeclarado();
        }
    }
}
