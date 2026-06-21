package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.EstadoPartidaResponse;
import com.transer.infiltrado.partida.application.port.PartidaEventPublisher;
import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.JugadorNoEncontradoException;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RegistrarSenalamienatoUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarSenalamienatoUseCase.class);

    private final PartidaRepository          partidaRepository;
    private final SenalamienatoRepository    senalamienatoRepository;
    private final PartidaEventPublisher      eventPublisher;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;

    public RegistrarSenalamienatoUseCase(PartidaRepository partidaRepository,
                                          SenalamienatoRepository senalamienatoRepository,
                                          PartidaEventPublisher eventPublisher,
                                          ObtenerEstadoPartidaUseCase obtenerEstado) {
        this.partidaRepository       = partidaRepository;
        this.senalamienatoRepository = senalamienatoRepository;
        this.eventPublisher          = eventPublisher;
        this.obtenerEstado           = obtenerEstado;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idUsuario, List<UUID> idsSenalados) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        Jugador jugador = partida.buscarJugadorPorUsuario(idUsuario)
                .orElseThrow(() -> new JugadorNoEncontradoException(idUsuario));

        EstadoPartida estadoAntes = partida.getEstado();

        // El dominio valida estado, auto-señalamiento y que los objetivos existan
        partida.registrarSenalamiento(jugador.getId(), idsSenalados);

        // Persistir un Senalamiento por cada objetivo
        for (UUID idSenalado : idsSenalados) {
            senalamienatoRepository.guardar(
                    Senalamiento.nuevo(partida.getId(), jugador.getId(), idSenalado));
        }

        partidaRepository.guardar(partida);

        int pendientes = (int) partida.getJugadores().stream()
                .filter(j -> !j.isHaSenalado()).count();

        eventPublisher.publicarProgresoSenalamiento(
                partida.getId(), jugador.getId(), jugador.getNombre(), pendientes);

        if (partida.getEstado() != estadoAntes) {
            eventPublisher.publicarCambioFase(
                    partida.getId(), partida.getEstado(), partida.getRondaActual());
        }

        log.info("Senalamiento registrado partida={} jugador={} objetivos={} pendientes={}",
                partida.getId(), jugador.getOrdenTurno(), idsSenalados.size(), pendientes);

        return obtenerEstado.construirRespuesta(partida);
    }
}
