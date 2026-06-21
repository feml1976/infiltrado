package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.partida.application.dto.RevelacionResponse;
import com.transer.infiltrado.partida.application.port.DatosRevelacion;
import com.transer.infiltrado.partida.application.port.JugadorRevelacionDto;
import com.transer.infiltrado.partida.application.port.PartidaEventPublisher;
import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.PartidaNoEncontradaException;
import com.transer.infiltrado.partida.domain.exception.TransicionInvalidaException;
import com.transer.infiltrado.partida.domain.RevisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Orquesta el cálculo de puntajes, su persistencia y la difusión del evento revelacion.
 * Es idempotente: si se llama más de una vez en estado REVELACION, el cálculo se salta
 * (detectado por adivinanzas con acierto ya evaluado) pero el evento WS se re-emite.
 */
@Service
public class RevelacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevelacionUseCase.class);

    private final PartidaRepository              partidaRepository;
    private final SenalamienatoRepository        senalamienatoRepository;
    private final AdivinanzaRepository           adivinanzaRepository;
    private final RevisionRepository             revisionRepository;
    private final PuntuacionHistoricaRepository  puntuacionRepository;
    private final CosaRepository                 cosaRepository;
    private final CalcularPuntajeUseCase         calcularPuntaje;
    private final PartidaEventPublisher          eventPublisher;

    public RevelacionUseCase(PartidaRepository partidaRepository,
                              SenalamienatoRepository senalamienatoRepository,
                              AdivinanzaRepository adivinanzaRepository,
                              RevisionRepository revisionRepository,
                              PuntuacionHistoricaRepository puntuacionRepository,
                              CosaRepository cosaRepository,
                              CalcularPuntajeUseCase calcularPuntaje,
                              PartidaEventPublisher eventPublisher) {
        this.partidaRepository       = partidaRepository;
        this.senalamienatoRepository = senalamienatoRepository;
        this.adivinanzaRepository    = adivinanzaRepository;
        this.revisionRepository      = revisionRepository;
        this.puntuacionRepository    = puntuacionRepository;
        this.cosaRepository          = cosaRepository;
        this.calcularPuntaje         = calcularPuntaje;
        this.eventPublisher          = eventPublisher;
    }

    @Transactional
    public RevelacionResponse ejecutar(String codigoSala) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (partida.getEstado() != EstadoPartida.REVELACION) {
            throw new TransicionInvalidaException(
                    "revelacion solo disponible en estado REVELACION, estado actual: "
                    + partida.getEstado());
        }

        // Capturar ID antes de cualquier reasignación de partida
        UUID idPartida = partida.getId();

        Cosa cosa = cosaRepository.buscarPorId(partida.getIdCosaActual())
                .orElseThrow(() -> new IllegalStateException("cosa no encontrada: " + idPartida));

        List<Senalamiento>  senalamientos = senalamienatoRepository.buscarPorPartida(idPartida);
        List<Revision>      revisiones    = revisionRepository.buscarPorPartida(idPartida);
        List<Adivinanza>    adivinanzas   = adivinanzaRepository.buscarPorPartida(idPartida);

        boolean yaCalculado = !adivinanzas.isEmpty()
                && adivinanzas.stream().allMatch(a -> a.getAcierto() != null);

        Map<UUID, Integer> deltas = new HashMap<>();
        for (Jugador j : partida.getJugadores()) {
            deltas.put(j.getId(), 0);
        }

        if (!yaCalculado) {
            CalcularPuntajeUseCase.Resultado resultado = calcularPuntaje.calcular(
                    partida.getJugadores(), cosa.getNombre(), senalamientos, adivinanzas, revisiones);

            // Actualizar adivinanzas con el acierto evaluado
            List<Adivinanza> actualizadas = new ArrayList<>();
            for (Adivinanza a : adivinanzas) {
                Boolean acierto = resultado.aciertosPorInfiltrado()
                        .getOrDefault(a.getIdJugadorInfiltrado(), false);
                Adivinanza act = a.conAcierto(acierto);
                adivinanzaRepository.guardar(act);
                actualizadas.add(act);
            }
            adivinanzas = actualizadas;

            // Aplicar deltas al agregado de dominio
            for (Map.Entry<UUID, Integer> e : resultado.deltasPorJugador().entrySet()) {
                partida.sumarPuntos(e.getKey(), e.getValue());
                deltas.put(e.getKey(), e.getValue());
            }

            partida = partidaRepository.guardar(partida);

            // Persistir puntuaciones históricas (upsert para idempotencia en CONTINUAR)
            for (Jugador j : partida.getJugadores()) {
                puntuacionRepository.upsertPuntos(j.getIdUsuario(), idPartida,
                        j.getPuntosAcumulados());
            }

            log.info("Puntajes calculados partida={} jugadores={}", idPartida,
                    partida.getJugadores().size());
        } else {
            // Recomputar deltas para el payload de respuesta (sin efectos secundarios)
            CalcularPuntajeUseCase.Resultado recomputed = calcularPuntaje.calcular(
                    partida.getJugadores(), cosa.getNombre(), senalamientos, adivinanzas, revisiones);
            deltas.putAll(recomputed.deltasPorJugador());
            log.debug("Revelacion ya calculada para partida={}, re-emitiendo evento WS", idPartida);
        }

        // Emitir evento WS con datos completos (siempre, también en re-llamadas)
        Partida finalPartida = partida;
        Map<UUID, Integer> finalDeltas = deltas;

        List<JugadorRevelacionDto> jugDtos = new ArrayList<>();
        for (Jugador j : finalPartida.getJugadores()) {
            jugDtos.add(new JugadorRevelacionDto(
                    j.getId(), j.getNombre(), j.getRol(),
                    finalDeltas.getOrDefault(j.getId(), 0),
                    j.getPuntosAcumulados()));
        }

        eventPublisher.publicarRevelacion(idPartida,
                new DatosRevelacion(jugDtos, finalPartida.getIdCosaActual(),
                        cosa.getNombre(), senalamientos, adivinanzas, finalDeltas));

        // Construir respuesta REST
        List<RevelacionResponse.SenalamienatoItem> senItems = new ArrayList<>();
        for (Senalamiento s : senalamientos) {
            senItems.add(new RevelacionResponse.SenalamienatoItem(
                    s.getIdJugadorOrigen(), s.getIdJugadorSenalado()));
        }

        List<RevelacionResponse.AdivinanzaItem> adiItems = new ArrayList<>();
        for (Adivinanza a : adivinanzas) {
            if (a.getAcierto() != null) {
                adiItems.add(new RevelacionResponse.AdivinanzaItem(
                        a.getIdJugadorInfiltrado(), a.getTextoAdivinanza(),
                        Boolean.TRUE.equals(a.getAcierto())));
            }
        }

        List<RevelacionResponse.JugadorItem> jugItems = new ArrayList<>();
        List<Jugador> ordenados = new ArrayList<>(finalPartida.getJugadores());
        ordenados.sort(Comparator.comparingInt(Jugador::getOrdenTurno));
        for (Jugador j : ordenados) {
            jugItems.add(new RevelacionResponse.JugadorItem(
                    j.getId(), j.getNombre(), j.getOrdenTurno(),
                    j.getRol() != null ? j.getRol().name() : null,
                    finalDeltas.getOrDefault(j.getId(), 0),
                    j.getPuntosAcumulados()));
        }

        return new RevelacionResponse(
                idPartida, finalPartida.getCodigoSala(),
                cosa.getNombre(), cosa.getTipo().name(),
                jugItems, senItems, adiItems);
    }
}
