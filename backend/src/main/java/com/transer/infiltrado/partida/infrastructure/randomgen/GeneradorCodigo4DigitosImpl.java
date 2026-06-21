package com.transer.infiltrado.partida.infrastructure.randomgen;

import com.transer.infiltrado.partida.domain.GeneradorCodigo4Digitos;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Set;

@Component
public class GeneradorCodigo4DigitosImpl implements GeneradorCodigo4Digitos {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String generar(Set<String> codigosExistentes) {
        String codigo;
        do {
            int n = SECURE_RANDOM.nextInt(10_000);
            codigo = String.format("%04d", n);
        } while (codigosExistentes.contains(codigo));
        return codigo;
    }
}
