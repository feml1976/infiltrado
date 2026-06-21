package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.RevisionResponse;
import com.transer.infiltrado.partida.application.dto.VotoResumen;
import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.JugadorNoEncontradoException;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import com.transer.infiltrado.partida.domain.exception.TransicionInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProponerRevisionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProponerRevisionUseCase.class);

    // Ventana de revisión: abierta en EN_CURSO, SENALAMIENTO y ADIVINANZA.
    // Cierra automáticamente al entrar en REVELACION.
    private static final Set<EstadoPartida> ESTADOS_CON_REVISION =
            Set.of(EstadoPartida.EN_CURSO, EstadoPartida.SENALAMIENTO, EstadoPartida.ADIVINANZA);

    private final PartidaRepository  partidaRepository;
    private final RevisionRepository revisionRepository;

    public ProponerRevisionUseCase(PartidaRepository partidaRepository,
                                    RevisionRepository revisionRepository) {
        this.partidaRepository  = partidaRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional
    public RevisionResponse ejecutar(String codigoSala, UUID idSolicitante,
                                      TipoRevision tipo, UUID idJugadorAcusado) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        partida.buscarJugadorPorUsuario(idSolicitante)
                .orElseThrow(() -> new TransicionInvalidaException("No eres jugador de esta partida"));

        if (!ESTADOS_CON_REVISION.contains(partida.getEstado())) {
            throw new TransicionInvalidaException(
                    "No se pueden proponer revisiones en estado " + partida.getEstado());
        }

        boolean acusadoValido = partida.getJugadores().stream()
                .anyMatch(j -> j.getId().equals(idJugadorAcusado));
        if (!acusadoValido) {
            throw new JugadorNoEncontradoException(idJugadorAcusado);
        }

        Revision revision = revisionRepository.guardar(
                Revision.nueva(partida.getId(), idJugadorAcusado, tipo));

        log.info("Revisión propuesta partida={} tipo={}", partida.getId(), tipo);

        return mapToResponse(revision);
    }

    static RevisionResponse mapToResponse(Revision revision) {
        List<VotoResumen> votos = revision.getVotos().stream()
                .map(v -> new VotoResumen(v.idJugador(), v.votoSi()))
                .toList();
        return new RevisionResponse(
                revision.getId(),
                revision.getIdPartida(),
                revision.getIdJugadorAcusado(),
                revision.getTipo(),
                revision.getEstado(),
                votos);
    }
}
