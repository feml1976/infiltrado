package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.catalogo.domain.exception.CosaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class DesactivarCosaUseCase {

    private static final Logger log = LoggerFactory.getLogger(DesactivarCosaUseCase.class);

    private final CosaRepository cosaRepository;

    public DesactivarCosaUseCase(CosaRepository cosaRepository) {
        this.cosaRepository = cosaRepository;
    }

    @Transactional
    public void ejecutar(UUID id) {
        if (cosaRepository.buscarPorId(id).isEmpty()) {
            throw new CosaNoEncontradaException();
        }
        cosaRepository.desactivar(id, Instant.now());
        log.info("Cosa desactivada id={}", id);
    }
}
