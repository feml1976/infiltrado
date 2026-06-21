package com.transer.infiltrado.partida.infrastructure.randomgen;

import com.transer.infiltrado.partida.application.port.GeneradorCodigoSala;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneradorCodigoSalaImpl implements GeneradorCodigoSala {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PartidaRepository partidaRepository;

    public GeneradorCodigoSalaImpl(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Override
    public String generar() {
        String codigo;
        do {
            codigo = generarCandidato();
        } while (partidaRepository.existePorCodigo(codigo));
        return codigo;
    }

    private String generarCandidato() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(SECURE_RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }
}
