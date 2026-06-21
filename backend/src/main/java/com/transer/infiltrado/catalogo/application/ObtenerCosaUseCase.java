package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.application.dto.CosaDetalleResponse;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.catalogo.domain.exception.CosaNoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObtenerCosaUseCase {

    private final CosaRepository cosaRepository;

    public ObtenerCosaUseCase(CosaRepository cosaRepository) {
        this.cosaRepository = cosaRepository;
    }

    @Transactional(readOnly = true)
    public CosaDetalleResponse ejecutar(UUID id) {
        return cosaRepository.buscarPorId(id)
                .map(CrearCosaUseCase::toDetalle)
                .orElseThrow(CosaNoEncontradaException::new);
    }
}
