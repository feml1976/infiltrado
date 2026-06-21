package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.EstadoPartidaResponse;
import com.transer.infiltrado.partida.domain.Partida;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnirseAPartidaUseCase {

    private static final Logger log = LoggerFactory.getLogger(UnirseAPartidaUseCase.class);

    private final PartidaRepository partidaRepository;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;

    public UnirseAPartidaUseCase(PartidaRepository partidaRepository,
                                  ObtenerEstadoPartidaUseCase obtenerEstado) {
        this.partidaRepository = partidaRepository;
        this.obtenerEstado     = obtenerEstado;
    }

    @Transactional
    public EstadoPartidaResponse ejecutar(String codigoSala, UUID idUsuario, String nombre) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        partida.unirJugador(idUsuario, nombre);
        partidaRepository.guardar(partida);

        log.info("Jugador unido a partida id={} jugadores={}", partida.getId(), partida.getJugadores().size());

        return obtenerEstado.construirRespuesta(partida);
    }
}
