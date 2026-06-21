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
public class AvanzarTurnoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AvanzarTurnoUseCase.class);

    private final PartidaRepository partidaRepository;
    private final PartidaEventPublisher eventPublisher;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;

    public AvanzarTurnoUseCase(PartidaRepository partidaRepository,
                                PartidaEventPublisher eventPublisher,
                                ObtenerEstadoPartidaUseCase obtenerEstado) {
        this.partidaRepository = partidaRepository;
        this.eventPublisher    = eventPublisher;
        this.obtenerEstado     = obtenerEstado;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idSolicitante) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        autorizarAvance(partida, idSolicitante);
        partida.avanzarTurno();
        partidaRepository.guardar(partida);

        publicarEvento(partida);

        log.info("Turno avanzado partida={} estado={} ronda={}",
                partida.getId(), partida.getEstado(), partida.getRondaActual());

        return obtenerEstado.construirRespuesta(partida);
    }

    private void autorizarAvance(Partida partida, UUID idSolicitante) {
        Jugador jugadorEnTurno = partida.getJugadorEnTurno();
        boolean esTurnoPropio = jugadorEnTurno != null
                && jugadorEnTurno.getIdUsuario().equals(idSolicitante);
        boolean esModerador   = partida.getIdModerador().equals(idSolicitante);

        if (!esTurnoPropio && !esModerador) {
            throw new TransicionInvalidaException("No es tu turno para avanzar");
        }
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
