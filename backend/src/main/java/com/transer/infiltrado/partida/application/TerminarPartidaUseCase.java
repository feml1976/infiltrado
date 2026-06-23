package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.EstadoPartidaResponse;
import com.transer.infiltrado.partida.application.port.PartidaEventPublisher;
import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import com.transer.infiltrado.partida.domain.exception.TransicionInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TerminarPartidaUseCase {

    private static final Logger log = LoggerFactory.getLogger(TerminarPartidaUseCase.class);

    private final PartidaRepository partidaRepository;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;
    private final PartidaEventPublisher eventPublisher;

    public TerminarPartidaUseCase(PartidaRepository partidaRepository,
                                   ObtenerEstadoPartidaUseCase obtenerEstado,
                                   PartidaEventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.obtenerEstado     = obtenerEstado;
        this.eventPublisher    = eventPublisher;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idSolicitante) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (!partida.getIdModerador().equals(idSolicitante)) {
            throw new TransicionInvalidaException("Solo el moderador puede terminar la partida");
        }

        partida.finalizar();
        partidaRepository.guardar(partida);

        log.info("Partida finalizada id={}", partida.getId());

        eventPublisher.publicarCambioFase(partida.getId(), EstadoPartida.FINALIZADA, partida.getRondaActual());

        return obtenerEstado.construirRespuesta(partida);
    }
}
