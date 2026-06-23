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
public class ContinuarPartidaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ContinuarPartidaUseCase.class);

    private final PartidaRepository          partidaRepository;
    private final AsignadorRoles             asignadorRoles;
    private final SelectorCosa               selectorCosa;
    private final PistaRepository            pistaRepository;
    private final SenalamienatoRepository    senalamienatoRepository;
    private final AdivinanzaRepository       adivinanzaRepository;
    private final RevisionRepository         revisionRepository;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;
    private final PartidaEventPublisher      eventPublisher;

    public ContinuarPartidaUseCase(PartidaRepository partidaRepository,
                                    AsignadorRoles asignadorRoles,
                                    SelectorCosa selectorCosa,
                                    PistaRepository pistaRepository,
                                    SenalamienatoRepository senalamienatoRepository,
                                    AdivinanzaRepository adivinanzaRepository,
                                    RevisionRepository revisionRepository,
                                    ObtenerEstadoPartidaUseCase obtenerEstado,
                                    PartidaEventPublisher eventPublisher) {
        this.partidaRepository       = partidaRepository;
        this.asignadorRoles          = asignadorRoles;
        this.selectorCosa            = selectorCosa;
        this.pistaRepository         = pistaRepository;
        this.senalamienatoRepository = senalamienatoRepository;
        this.adivinanzaRepository    = adivinanzaRepository;
        this.revisionRepository      = revisionRepository;
        this.obtenerEstado           = obtenerEstado;
        this.eventPublisher          = eventPublisher;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idSolicitante) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (!partida.getIdModerador().equals(idSolicitante)) {
            throw new TransicionInvalidaException("Solo el moderador puede continuar la partida");
        }

        UUID idPartida = partida.getId();

        // Limpiar datos de la ronda anterior: la constraint pistas_jugador_ronda_uq y la
        // lógica de scoring exigen que la nueva ronda empiece desde un estado limpio.
        pistaRepository.eliminarPorPartida(idPartida);
        senalamienatoRepository.eliminarPorPartida(idPartida);
        adivinanzaRepository.eliminarPorPartida(idPartida);
        revisionRepository.eliminarPorPartida(idPartida);

        partida.continuar(asignadorRoles, selectorCosa);
        partidaRepository.guardar(partida);

        log.info("Partida continuada id={} jugadores={}", idPartida, partida.getJugadores().size());

        eventPublisher.publicarCambioFase(idPartida, EstadoPartida.EN_CURSO, partida.getRondaActual());

        return obtenerEstado.construirRespuesta(partida);
    }
}
