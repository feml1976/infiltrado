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

    /**
     * Calcula puntajes, persiste aciertos y emite el evento WS.
     * Invocado UNA SOLA VEZ desde RegistrarAdivinanzaUseCase en la transición → REVELACION.
     * Nunca debe ser llamado desde el endpoint GET (evita doble conteo bajo concurrencia).
     */
    @Transactional
    public void calcularYPublicar(String codigoSala) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (partida.getEstado() != EstadoPartida.REVELACION) {
            throw new TransicionInvalidaException(
                    "revelacion solo disponible en estado REVELACION, estado actual: "
                    + partida.getEstado());
        }

        UUID idPartida = partida.getId();

        Cosa cosa = cosaRepository.buscarPorId(partida.getIdCosaActual())
                .orElseThrow(() -> new IllegalStateException("cosa no encontrada: " + idPartida));

        List<Senalamiento> senalamientos = senalamienatoRepository.buscarPorPartida(idPartida);
        List<Revision>     revisiones    = revisionRepository.buscarPorPartida(idPartida);
        List<Adivinanza>   adivinanzas   = adivinanzaRepository.buscarPorPartida(idPartida);

        CalcularPuntajeUseCase.Resultado resultado = calcularPuntaje.calcular(
                partida.getJugadores(), cosa.getNombre(), senalamientos, adivinanzas, revisiones);

        List<Adivinanza> actualizadas = new ArrayList<>();
        for (Adivinanza a : adivinanzas) {
            Boolean acierto = resultado.aciertosPorInfiltrado()
                    .getOrDefault(a.getIdJugadorInfiltrado(), false);
            Adivinanza act = a.conAcierto(acierto);
            adivinanzaRepository.guardar(act);
            actualizadas.add(act);
        }

        Map<UUID, Integer> deltas = new HashMap<>();
        partida.getJugadores().forEach(j -> deltas.put(j.getId(), 0));
        for (Map.Entry<UUID, Integer> e : resultado.deltasPorJugador().entrySet()) {
            partida.sumarPuntos(e.getKey(), e.getValue());
            deltas.put(e.getKey(), e.getValue());
        }

        partida = partidaRepository.guardar(partida);

        for (Jugador j : partida.getJugadores()) {
            puntuacionRepository.upsertPuntos(j.getIdUsuario(), idPartida, j.getPuntosAcumulados());
        }

        log.info("Puntajes calculados partida={} jugadores={}", idPartida, partida.getJugadores().size());

        emitirEvento(idPartida, partida, cosa, senalamientos, actualizadas, deltas);
    }

    /**
     * Devuelve los datos de revelación para el endpoint GET /{codigoSala}/revelacion.
     * Solo lectura: nunca llama a sumarPuntos() ni persiste, seguro bajo cualquier concurrencia.
     * Re-emite el evento WS para clientes que llegaron a la pantalla después del evento inicial.
     */
    @Transactional(readOnly = true)
    public RevelacionResponse ejecutar(String codigoSala) {
        Partida partida = partidaRepository.buscarPorCodigo(codigoSala)
                .orElseThrow(() -> new PartidaNoEncontradaException(codigoSala));

        if (partida.getEstado() != EstadoPartida.REVELACION) {
            throw new TransicionInvalidaException(
                    "revelacion solo disponible en estado REVELACION, estado actual: "
                    + partida.getEstado());
        }

        UUID idPartida = partida.getId();

        Cosa cosa = cosaRepository.buscarPorId(partida.getIdCosaActual())
                .orElseThrow(() -> new IllegalStateException("cosa no encontrada: " + idPartida));

        List<Senalamiento> senalamientos = senalamienatoRepository.buscarPorPartida(idPartida);
        List<Revision>     revisiones    = revisionRepository.buscarPorPartida(idPartida);
        List<Adivinanza>   adivinanzas   = adivinanzaRepository.buscarPorPartida(idPartida);

        Map<UUID, Integer> deltas = new HashMap<>();
        partida.getJugadores().forEach(j -> deltas.put(j.getId(), 0));
        CalcularPuntajeUseCase.Resultado resultado = calcularPuntaje.calcular(
                partida.getJugadores(), cosa.getNombre(), senalamientos, adivinanzas, revisiones);
        deltas.putAll(resultado.deltasPorJugador());

        log.debug("GET revelacion (solo lectura) partida={}", idPartida);

        emitirEvento(idPartida, partida, cosa, senalamientos, adivinanzas, deltas);

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
        List<Jugador> ordenados = new ArrayList<>(partida.getJugadores());
        ordenados.sort(Comparator.comparingInt(Jugador::getOrdenTurno));
        for (Jugador j : ordenados) {
            jugItems.add(new RevelacionResponse.JugadorItem(
                    j.getId(), j.getNombre(), j.getOrdenTurno(),
                    j.getRol() != null ? j.getRol().name() : null,
                    deltas.getOrDefault(j.getId(), 0),
                    j.getPuntosAcumulados()));
        }

        return new RevelacionResponse(
                idPartida, partida.getCodigoSala(),
                cosa.getNombre(), cosa.getTipo().name(),
                jugItems, senItems, adiItems);
    }

    private void emitirEvento(UUID idPartida, Partida partida, Cosa cosa,
                               List<Senalamiento> senalamientos, List<Adivinanza> adivinanzas,
                               Map<UUID, Integer> deltas) {
        List<JugadorRevelacionDto> jugDtos = new ArrayList<>();
        for (Jugador j : partida.getJugadores()) {
            jugDtos.add(new JugadorRevelacionDto(
                    j.getId(), j.getNombre(), j.getRol(),
                    deltas.getOrDefault(j.getId(), 0),
                    j.getPuntosAcumulados()));
        }
        eventPublisher.publicarRevelacion(idPartida,
                new DatosRevelacion(jugDtos, partida.getIdCosaActual(),
                        cosa.getNombre(), senalamientos, adivinanzas, deltas));
    }
}
