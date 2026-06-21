package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.RevisionResponse;
import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import com.transer.infiltrado.partida.domain.exception.RevisionNoEncontradaException;
import com.transer.infiltrado.partida.domain.exception.TransicionInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VotarRevisionUseCase {

    private static final Logger log = LoggerFactory.getLogger(VotarRevisionUseCase.class);

    private final PartidaRepository  partidaRepository;
    private final RevisionRepository revisionRepository;

    public VotarRevisionUseCase(PartidaRepository partidaRepository,
                                 RevisionRepository revisionRepository) {
        this.partidaRepository  = partidaRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional
    public RevisionResponse ejecutar(String codigoSala, UUID idRevision,
                                      UUID idVotante, boolean votoSi) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        Jugador jugador = partida.buscarJugadorPorUsuario(idVotante)
                .orElseThrow(() -> new TransicionInvalidaException("No eres jugador de esta partida"));

        Revision revision = revisionRepository.buscarPorId(idRevision)
                .orElseThrow(() -> new RevisionNoEncontradaException(idRevision));

        if (!revision.getIdPartida().equals(partida.getId())) {
            throw new RevisionNoEncontradaException(idRevision);
        }

        revision.registrarVoto(jugador.getId(), votoSi, partida.getJugadores().size());
        revisionRepository.guardar(revision);

        log.info("Voto registrado revision={} cerrada={}", idRevision, revision.estaCerrada());

        return ProponerRevisionUseCase.mapToResponse(revision);
    }
}
