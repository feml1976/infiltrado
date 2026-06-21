package com.transer.infiltrado.partida.infrastructure.randomgen;

import com.transer.infiltrado.partida.domain.AsignadorRoles;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AsignadorRolesAleatorio implements AsignadorRoles {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public Set<Integer> seleccionar(int numJugadores, int numInfiltrados) {
        List<Integer> posiciones = new ArrayList<>();
        for (int i = 1; i <= numJugadores; i++) posiciones.add(i);
        Collections.shuffle(posiciones, SECURE_RANDOM);

        Set<Integer> infiltrados = new HashSet<>();
        for (int i = 0; i < numInfiltrados; i++) infiltrados.add(posiciones.get(i));
        return infiltrados;
    }
}
