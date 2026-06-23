package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.AcumuladoResponse;
import com.transer.infiltrado.partida.domain.PuntuacionHistoricaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObtenerAcumuladoGlobalUseCase {

    private final PuntuacionHistoricaRepository puntuacionRepository;

    public ObtenerAcumuladoGlobalUseCase(PuntuacionHistoricaRepository puntuacionRepository) {
        this.puntuacionRepository = puntuacionRepository;
    }

    @Transactional(readOnly = true)
    public AcumuladoResponse ejecutar(UUID idUsuario) {
        int total = puntuacionRepository.acumuladoGlobal(idUsuario);
        return new AcumuladoResponse(total);
    }
}
