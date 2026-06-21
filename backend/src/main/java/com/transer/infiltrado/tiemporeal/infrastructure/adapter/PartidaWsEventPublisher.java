package com.transer.infiltrado.tiemporeal.infrastructure.adapter;

import com.transer.infiltrado.partida.application.port.DatosRevelacion;
import com.transer.infiltrado.partida.application.port.PartidaEventPublisher;
import com.transer.infiltrado.partida.domain.EstadoPartida;
import com.transer.infiltrado.tiemporeal.infrastructure.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador WS: implementa PartidaEventPublisher difundiendo eventos STOMP
 * al topic /topic/partida/{id}.
 *
 * Nota: convertAndSend es síncrono respecto al broker en memoria.
 * Para evitar que clientes lean estado inconsistente, los casos de uso que publican
 * deben asegurarse de que la transacción haya comprometido antes de llamar aquí.
 * Ver patrón @TransactionalEventListener si se requiere garantía fuerte (Paso 16).
 */
@Component
public class PartidaWsEventPublisher implements PartidaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PartidaWsEventPublisher.class);

    private final SimpMessagingTemplate messaging;

    public PartidaWsEventPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Override
    public void publicarTurnoDe(UUID idPartida, UUID idJugador,
                                String nombreJugador, int ordenTurno, int rondaActual) {
        enviar(idPartida, "turno_de",
                new TurnoDePayload(idJugador, nombreJugador, ordenTurno, rondaActual));
        log.debug("WS turno_de partida={} jugador={}", idPartida, ordenTurno);
    }

    @Override
    public void publicarCambioFase(UUID idPartida, EstadoPartida nuevoEstado, int rondaActual) {
        enviar(idPartida, "cambio_fase",
                new CambioFasePayload(nuevoEstado.name(), rondaActual));
        log.debug("WS cambio_fase partida={} estado={}", idPartida, nuevoEstado);
    }

    @Override
    public void publicarPistaRegistrada(UUID idPartida, UUID idJugador, String nombreJugador) {
        enviar(idPartida, "pista_registrada",
                new PistaRegistradaPayload(idJugador, nombreJugador));
        log.debug("WS pista_registrada partida={}", idPartida);
    }

    @Override
    public void publicarRevelacion(UUID idPartida, DatosRevelacion datos) {
        List<RevelacionJugadorDto> jugDtos = datos.jugadores().stream()
                .map(j -> new RevelacionJugadorDto(j.idJugador(), j.nombre(), j.rol().name(),
                        j.deltaRonda(), j.puntosAcumulados()))
                .toList();
        List<SenalamienatoRevelacionDto> senDtos = datos.senalamientos().stream()
                .map(s -> new SenalamienatoRevelacionDto(
                        s.getIdJugadorOrigen(), s.getIdJugadorSenalado()))
                .toList();
        List<AdivinanzaRevelacionDto> adiDtos = datos.adivinanzas().stream()
                .filter(a -> a.getAcierto() != null)
                .map(a -> new AdivinanzaRevelacionDto(
                        a.getIdJugadorInfiltrado(), a.getTextoAdivinanza(),
                        Boolean.TRUE.equals(a.getAcierto())))
                .toList();
        enviar(idPartida, "revelacion",
                new RevelacionPayload(jugDtos, datos.idCosa(), datos.nombreCosa(), senDtos, adiDtos));
        log.debug("WS revelacion partida={}", idPartida);
    }

    @Override
    public void publicarProgresoSenalamiento(UUID idPartida, UUID idJugador,
                                              String nombre, int pendientes) {
        enviar(idPartida, "progreso_senalamiento",
                new ProgresoSenalamienatoPayload(idJugador, nombre, pendientes));
        log.debug("WS progreso_senalamiento partida={} pendientes={}", idPartida, pendientes);
    }

    @Override
    public void publicarProgresoAdivinanza(UUID idPartida, int pendientes) {
        enviar(idPartida, "progreso_adivinanza",
                new ProgresoAdivinanzaPayload(pendientes));
        log.debug("WS progreso_adivinanza partida={} pendientes={}", idPartida, pendientes);
    }

    private void enviar(UUID idPartida, String tipo, Object datos) {
        messaging.convertAndSend("/topic/partida/" + idPartida, new WsEnvelope(tipo, datos));
    }
}
