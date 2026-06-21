package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.application.dto.CosaResumenResponse;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarCosasUseCase {

    private final CosaRepository cosaRepository;

    public ListarCosasUseCase(CosaRepository cosaRepository) {
        this.cosaRepository = cosaRepository;
    }

    @Transactional(readOnly = true)
    public List<CosaResumenResponse> ejecutar() {
        return cosaRepository.listarActivos().stream()
                .map(c -> new CosaResumenResponse(c.getId(), c.getNombre(),
                        c.getTipo(), c.isActivo(), c.getCreadoEn()))
                .toList();
    }
}
