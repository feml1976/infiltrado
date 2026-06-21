package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.EstadoPartidaResponse;
import com.transer.infiltrado.partida.application.dto.JugadorResumenResponse;
import com.transer.infiltrado.partida.domain.Jugador;
import com.transer.infiltrado.partida.domain.Partida;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerEstadoPartidaUseCase {

    private final PartidaRepository partidaRepository;

    public ObtenerEstadoPartidaUseCase(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Transactional(readOnly = true)
    public EstadoPartidaResponse ejecutar(String codigoSala) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));
        return construirRespuesta(partida);
    }

    EstadoPartidaResponse construirRespuesta(Partida partida) {
        List<JugadorResumenResponse> jugadores = partida.getJugadores().stream()
                .map(this::mapJugador)
                .toList();

        return new EstadoPartidaResponse(
                partida.getId(),
                partida.getCodigoSala(),
                partida.getIdModerador(),
                partida.getEstado(),
                partida.getNumRondasTotal(),
                partida.getNumInfiltrados(),
                partida.getNumJugadores(),
                partida.getRondaActual(),
                jugadores);
    }

    private JugadorResumenResponse mapJugador(Jugador j) {
        return new JugadorResumenResponse(
                j.getId(), j.getNombre(), j.getOrdenTurno(),
                j.getPuntosAcumulados(), j.isHaSenalado());
    }
}
