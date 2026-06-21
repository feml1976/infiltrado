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
public class IniciarPartidaUseCase {

    private static final Logger log = LoggerFactory.getLogger(IniciarPartidaUseCase.class);

    private final PartidaRepository partidaRepository;
    private final AsignadorRoles asignadorRoles;
    private final SelectorCosa selectorCosa;
    private final GeneradorCodigo4Digitos generadorCodigo;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;
    private final PartidaEventPublisher eventPublisher;

    public IniciarPartidaUseCase(PartidaRepository partidaRepository,
                                  AsignadorRoles asignadorRoles,
                                  SelectorCosa selectorCosa,
                                  GeneradorCodigo4Digitos generadorCodigo,
                                  ObtenerEstadoPartidaUseCase obtenerEstado,
                                  PartidaEventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.asignadorRoles    = asignadorRoles;
        this.selectorCosa      = selectorCosa;
        this.generadorCodigo   = generadorCodigo;
        this.obtenerEstado     = obtenerEstado;
        this.eventPublisher    = eventPublisher;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idSolicitante) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (!partida.getIdModerador().equals(idSolicitante)) {
            throw new TransicionInvalidaException("Solo el moderador puede iniciar la partida");
        }

        partida.iniciar(asignadorRoles, selectorCosa, generadorCodigo);
        partidaRepository.guardar(partida);

        log.info("Partida iniciada id={} jugadores={}", partida.getId(), partida.getJugadores().size());

        eventPublisher.publicarCambioFase(partida.getId(), EstadoPartida.EN_CURSO, partida.getRondaActual());

        return obtenerEstado.construirRespuesta(partida);
    }
}
