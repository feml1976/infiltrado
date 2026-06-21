package com.transer.infiltrado.partida.domain;

import java.util.Set;

/**
 * Puerto de dominio para la asignación aleatoria de roles.
 * La implementación concreta (aleatoria real) se provee en el Paso 7.
 *
 * @return conjunto de valores de ordenTurno (1-indexed) que recibirán INFILTRADO
 */
public interface AsignadorRoles {
    Set<Integer> seleccionar(int numJugadores, int numInfiltrados);
}
