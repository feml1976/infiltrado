package com.transer.infiltrado.partida.application.port;

import com.transer.infiltrado.partida.domain.EstadoPartida;

import java.util.UUID;

public interface PartidaEventPublisher {

    /** Turno del jugador con idJugador. No incluye rol ni cosa. */
    void publicarTurnoDe(UUID idPartida, UUID idJugador, String nombreJugador, int ordenTurno, int rondaActual);

    /** Cambio de fase (LOBBY→EN_CURSO, EN_CURSO→SENALAMIENTO, etc.). No incluye rol ni cosa. */
    void publicarCambioFase(UUID idPartida, EstadoPartida nuevoEstado, int rondaActual);

    /** Señal de que un jugador registró su pista en turno. Solo identificador, sin rol ni contenido. */
    void publicarPistaRegistrada(UUID idPartida, UUID idJugador, String nombreJugador);

    /** Solo se emite cuando estado == REVELACION. Incluye roles, cosa, aciertos y desglose de puntos. */
    void publicarRevelacion(UUID idPartida, DatosRevelacion datos);

    /** Progreso de señalamiento: quién acaba de señalar y cuántos quedan. Sin rol ni cosa. */
    void publicarProgresoSenalamiento(UUID idPartida, UUID idJugador, String nombre, int pendientes);

    /** Progreso de adivinanza: cuántos infiltrados quedan por declarar. Sin nombre ni rol. */
    void publicarProgresoAdivinanza(UUID idPartida, int pendientes);
}
