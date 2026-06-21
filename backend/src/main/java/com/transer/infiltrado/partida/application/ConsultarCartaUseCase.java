package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.partida.application.dto.CartaResponse;
import com.transer.infiltrado.partida.domain.Jugador;
import com.transer.infiltrado.partida.domain.Partida;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import com.transer.infiltrado.partida.domain.exception.CartaAccesoDenegadoException;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConsultarCartaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCartaUseCase.class);

    private final PartidaRepository partidaRepository;
    private final CosaRepository cosaRepository;

    public ConsultarCartaUseCase(PartidaRepository partidaRepository, CosaRepository cosaRepository) {
        this.partidaRepository = partidaRepository;
        this.cosaRepository    = cosaRepository;
    }

    @Transactional(readOnly = true)
    public CartaResponse ejecutar(String codigoSala, UUID idUsuario) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> {
                    // Anti-enumeración: no revelar si la sala existe o no; tratarlo igual que "no eres jugador"
                    log.warn("Intento de carta en sala inexistente usuario={}", idUsuario);
                    return new CartaAccesoDenegadoException();
                });

        Jugador jugador = partida.buscarJugadorPorUsuario(idUsuario)
                .orElseThrow(() -> {
                    log.warn("Intento de carta sin membresía en partida={} usuario={}", partida.getId(), idUsuario);
                    return new CartaAccesoDenegadoException();
                });

        if (jugador.getRol() == null) {
            // Partida aún en LOBBY: rol y cosa no asignados todavía
            return new CartaResponse(null, null, null, null, null);
        }

        UUID idCosa = partida.getIdCosaActual();
        Cosa cosa = cosaRepository.buscarPorId(idCosa).orElse(null);

        return new CartaResponse(
                jugador.getRol(),
                idCosa,
                cosa != null ? cosa.getNombre() : null,
                cosa != null ? cosa.getTipo().name() : null,
                cosa != null ? cosa.getImagenBase64() : null);
    }
}
