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
public class RegistrarPistaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarPistaUseCase.class);

    private final PartidaRepository         partidaRepository;
    private final PistaRepository           pistaRepository;
    private final PartidaEventPublisher     eventPublisher;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;

    public RegistrarPistaUseCase(PartidaRepository partidaRepository,
                                  PistaRepository pistaRepository,
                                  PartidaEventPublisher eventPublisher,
                                  ObtenerEstadoPartidaUseCase obtenerEstado) {
        this.partidaRepository = partidaRepository;
        this.pistaRepository   = pistaRepository;
        this.eventPublisher    = eventPublisher;
        this.obtenerEstado     = obtenerEstado;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idSolicitante, String contenido) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        Jugador enTurno = partida.getJugadorEnTurno();
        if (enTurno == null || !enTurno.getIdUsuario().equals(idSolicitante)) {
            throw new TransicionInvalidaException("No es tu turno para registrar una pista");
        }

        // Persistir pista antes de avanzar para capturar ronda/orden actuales
        Pista pista = Pista.nueva(partida.getId(), enTurno.getId(),
                partida.getRondaActual(), enTurno.getOrdenTurno(), contenido);
        pistaRepository.guardar(pista);

        eventPublisher.publicarPistaRegistrada(partida.getId(), enTurno.getId(), enTurno.getNombre());

        partida.avanzarTurno();
        partidaRepository.guardar(partida);

        publicarEvento(partida);

        log.info("Pista registrada partida={} ronda={} estado={}",
                partida.getId(), partida.getRondaActual(), partida.getEstado());

        return obtenerEstado.construirRespuesta(partida);
    }

    private void publicarEvento(Partida partida) {
        if (partida.getEstado() == EstadoPartida.EN_CURSO) {
            Jugador siguiente = partida.getJugadorEnTurno();
            eventPublisher.publicarTurnoDe(
                    partida.getId(),
                    siguiente.getId(),
                    siguiente.getNombre(),
                    siguiente.getOrdenTurno(),
                    partida.getRondaActual());
        } else {
            eventPublisher.publicarCambioFase(
                    partida.getId(),
                    partida.getEstado(),
                    partida.getRondaActual());
        }
    }
}
