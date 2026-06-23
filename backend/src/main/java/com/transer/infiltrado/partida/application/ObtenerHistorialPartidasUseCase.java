package com.transer.infiltrado.partida.application;

import com.transer.infiltrado.partida.application.dto.HistorialResponse;
import com.transer.infiltrado.partida.domain.PuntuacionHistoricaEntry;
import com.transer.infiltrado.partida.domain.PuntuacionHistoricaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ObtenerHistorialPartidasUseCase {

    private final PuntuacionHistoricaRepository puntuacionRepository;

    public ObtenerHistorialPartidasUseCase(PuntuacionHistoricaRepository puntuacionRepository) {
        this.puntuacionRepository = puntuacionRepository;
    }

    @Transactional(readOnly = true)
    public HistorialResponse ejecutar(UUID idUsuario) {
        List<PuntuacionHistoricaEntry> entradas = puntuacionRepository.buscarHistorialPorUsuario(idUsuario);
        List<HistorialResponse.Item> items = entradas.stream()
                .map(e -> new HistorialResponse.Item(e.idPartida(), e.puntos(), e.fecha()))
                .toList();
        return new HistorialResponse(items);
    }
}
