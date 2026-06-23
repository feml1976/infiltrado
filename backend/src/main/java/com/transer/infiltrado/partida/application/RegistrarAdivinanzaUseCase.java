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

import java.util.UUID;

@Service
public class RegistrarAdivinanzaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarAdivinanzaUseCase.class);

    private final PartidaRepository           partidaRepository;
    private final AdivinanzaRepository        adivinanzaRepository;
    private final PartidaEventPublisher       eventPublisher;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;
    private final RevelacionUseCase           revelacionUseCase;

    public RegistrarAdivinanzaUseCase(PartidaRepository partidaRepository,
                                       AdivinanzaRepository adivinanzaRepository,
                                       PartidaEventPublisher eventPublisher,
                                       ObtenerEstadoPartidaUseCase obtenerEstado,
                                       RevelacionUseCase revelacionUseCase) {
        this.partidaRepository   = partidaRepository;
        this.adivinanzaRepository = adivinanzaRepository;
        this.eventPublisher      = eventPublisher;
        this.obtenerEstado       = obtenerEstado;
        this.revelacionUseCase   = revelacionUseCase;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idUsuario, String textoAdivinanza) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        Jugador jugador = partida.buscarJugadorPorUsuario(idUsuario)
                .orElseThrow(() -> new JugadorNoEncontradoException(idUsuario));

        EstadoPartida estadoAntes = partida.getEstado();

        // El dominio valida estado=ADIVINANZA, que el jugador es INFILTRADO y que no haya declarado ya
        partida.registrarDeclaracion(jugador.getId());

        adivinanzaRepository.guardar(
                Adivinanza.nueva(partida.getId(), jugador.getId(), textoAdivinanza));

        partidaRepository.guardar(partida);

        int pendientes = (int) partida.getJugadores().stream()
                .filter(j -> j.getRol() == RolJugador.INFILTRADO && !j.isHaDeclarado()).count();

        // Solo informamos cuántos infiltrados quedan — no exponemos quién declaró (evita revelar rol)
        eventPublisher.publicarProgresoAdivinanza(partida.getId(), pendientes);

        if (partida.getEstado() != estadoAntes) {
            eventPublisher.publicarCambioFase(
                    partida.getId(), partida.getEstado(), partida.getRondaActual());
            if (partida.getEstado() == EstadoPartida.REVELACION) {
                // Calcular puntajes, persistir y emitir evento revelacion WS (una sola vez en esta TX)
                revelacionUseCase.calcularYPublicar(partida.getCodigoSala());
            }
        }

        log.info("Adivinanza registrada partida={} jugador={} pendientes={}",
                partida.getId(), jugador.getOrdenTurno(), pendientes);

        return obtenerEstado.construirRespuesta(partida);
    }
}
