package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.domain.*;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

/**
 * Servicio puro: calcula el delta de puntos por jugador y el acierto de cada infiltrado.
 * No tiene dependencias de repositorios ni efectos secundarios.
 *
 * Reglas aplicadas:
 *  R1. +10 al infiltrado que adivinó la cosa (comparación normalizada).
 *  R2. +10 al infiltrado no descubierto (nadie lo señaló).
 *  R3. +10 al señalador por cada infiltrado correctamente señalado.
 *  R4. -10 al señalador por cada no-infiltrado señalado por error.
 *  R5. -10 al acusado por cada revisión NOMBRAR_COSA + ROMPIO.
 *  R6. -5  al acusado por cada revisión PISTA_SOSPECHOSA + ROMPIO.
 */
@Service
public class CalcularPuntajeUseCase {

    public record Resultado(
            Map<UUID, Integer> deltasPorJugador,
            Map<UUID, Boolean> aciertosPorInfiltrado
    ) {}

    public Resultado calcular(List<Jugador> jugadores,
                               String nombreCosa,
                               List<Senalamiento> senalamientos,
                               List<Adivinanza> adivinanzas,
                               List<Revision> revisiones) {

        Map<UUID, Integer> deltas = new HashMap<>();
        Map<UUID, Boolean> aciertos = new HashMap<>();

        jugadores.forEach(j -> deltas.put(j.getId(), 0));

        Set<UUID> idsInfiltrados = new HashSet<>();
        jugadores.stream()
                .filter(j -> j.getRol() == RolJugador.INFILTRADO)
                .map(Jugador::getId)
                .forEach(idsInfiltrados::add);

        String cosaNorm = normalizar(nombreCosa);

        // R1: +10 al infiltrado que adivinó
        for (Adivinanza a : adivinanzas) {
            boolean acierto = normalizar(a.getTextoAdivinanza()).equals(cosaNorm);
            aciertos.put(a.getIdJugadorInfiltrado(), acierto);
            if (acierto) {
                deltas.merge(a.getIdJugadorInfiltrado(), 10, Integer::sum);
            }
        }

        // R2: +10 al infiltrado no descubierto
        Set<UUID> infiltradosDescubiertos = new HashSet<>();
        for (Senalamiento s : senalamientos) {
            if (idsInfiltrados.contains(s.getIdJugadorSenalado())) {
                infiltradosDescubiertos.add(s.getIdJugadorSenalado());
            }
        }
        for (UUID idInfiltrado : idsInfiltrados) {
            if (!infiltradosDescubiertos.contains(idInfiltrado)) {
                deltas.merge(idInfiltrado, 10, Integer::sum);
            }
        }

        // R3 y R4: +10 por señalar a un infiltrado, -10 por señalar a un no-infiltrado
        for (Senalamiento s : senalamientos) {
            if (idsInfiltrados.contains(s.getIdJugadorSenalado())) {
                deltas.merge(s.getIdJugadorOrigen(), 10, Integer::sum);
            } else {
                deltas.merge(s.getIdJugadorOrigen(), -10, Integer::sum);
            }
        }

        // R5 y R6: penalizaciones por revisiones ROMPIO
        for (Revision r : revisiones) {
            if (r.getEstado() == EstadoRevision.ROMPIO) {
                if (r.getTipo() == TipoRevision.NOMBRAR_COSA) {
                    deltas.merge(r.getIdJugadorAcusado(), -10, Integer::sum);
                } else if (r.getTipo() == TipoRevision.PISTA_SOSPECHOSA) {
                    deltas.merge(r.getIdJugadorAcusado(), -5, Integer::sum);
                }
            }
        }

        return new Resultado(Collections.unmodifiableMap(deltas),
                             Collections.unmodifiableMap(aciertos));
    }

    static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
    }
}
