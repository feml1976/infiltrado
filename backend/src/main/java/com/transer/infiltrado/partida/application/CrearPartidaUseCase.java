package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.CrearPartidaRequest;
import com.transer.infiltrado.partida.application.dto.CrearPartidaResponse;
import com.transer.infiltrado.partida.application.port.GeneradorCodigoSala;
import com.transer.infiltrado.partida.domain.Partida;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearPartidaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearPartidaUseCase.class);

    private final PartidaRepository partidaRepository;
    private final GeneradorCodigoSala generadorCodigoSala;

    public CrearPartidaUseCase(PartidaRepository partidaRepository,
                               GeneradorCodigoSala generadorCodigoSala) {
        this.partidaRepository   = partidaRepository;
        this.generadorCodigoSala = generadorCodigoSala;
    }

    @Transactional
    public CrearPartidaResponse ejecutar(UUID idModerador, CrearPartidaRequest request) {
        String codigoSala = generadorCodigoSala.generar();

        Partida partida = Partida.crear(
                idModerador,
                codigoSala,
                request.numRondas(),
                request.numInfiltrados(),
                request.numJugadores());

        Partida guardada = partidaRepository.guardar(partida);

        log.info("Partida creada id={} moderador={} rondas={} infiltrados={} cupo={}",
                guardada.getId(), idModerador,
                request.numRondas(), request.numInfiltrados(), request.numJugadores());

        return new CrearPartidaResponse(
                guardada.getId(),
                guardada.getCodigoSala(),
                guardada.getEstado().name());
    }
}
