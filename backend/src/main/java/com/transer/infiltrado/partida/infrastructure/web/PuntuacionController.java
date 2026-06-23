package com.transer.infiltrado.partida.infrastructure.web;

import com.transer.infiltrado.partida.application.ObtenerAcumuladoGlobalUseCase;
import com.transer.infiltrado.partida.application.ObtenerHistorialPartidasUseCase;
import com.transer.infiltrado.partida.application.dto.AcumuladoResponse;
import com.transer.infiltrado.partida.application.dto.HistorialResponse;
import com.transer.infiltrado.shared.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class PuntuacionController {

    private final ObtenerAcumuladoGlobalUseCase obtenerAcumulado;
    private final ObtenerHistorialPartidasUseCase obtenerHistorial;

    public PuntuacionController(ObtenerAcumuladoGlobalUseCase obtenerAcumulado,
                                 ObtenerHistorialPartidasUseCase obtenerHistorial) {
        this.obtenerAcumulado  = obtenerAcumulado;
        this.obtenerHistorial  = obtenerHistorial;
    }

    @GetMapping("/acumulado")
    public AcumuladoResponse acumulado(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return obtenerAcumulado.ejecutar(principal.getId());
    }

    @GetMapping("/historial")
    public HistorialResponse historial(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return obtenerHistorial.ejecutar(principal.getId());
    }
}
