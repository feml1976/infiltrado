package com.transer.infiltrado.catalogo.infrastructure.web;

import com.transer.infiltrado.catalogo.application.*;
import com.transer.infiltrado.catalogo.application.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogo/cosas")
@PreAuthorize("hasRole('ADMIN')")
public class CatalogoController {

    private final CrearCosaUseCase crear;
    private final ActualizarCosaUseCase actualizar;
    private final DesactivarCosaUseCase desactivar;
    private final ListarCosasUseCase listar;
    private final ObtenerCosaUseCase obtener;

    public CatalogoController(CrearCosaUseCase crear,
                               ActualizarCosaUseCase actualizar,
                               DesactivarCosaUseCase desactivar,
                               ListarCosasUseCase listar,
                               ObtenerCosaUseCase obtener) {
        this.crear      = crear;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
        this.listar     = listar;
        this.obtener    = obtener;
    }

    @PostMapping
    public ResponseEntity<CosaDetalleResponse> crear(@Valid @RequestBody CrearCosaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crear.ejecutar(request));
    }

    @GetMapping
    public List<CosaResumenResponse> listar() {
        return listar.ejecutar();
    }

    @GetMapping("/{id}")
    public CosaDetalleResponse obtener(@PathVariable UUID id) {
        return obtener.ejecutar(id);
    }

    @PutMapping("/{id}")
    public CosaDetalleResponse actualizar(@PathVariable UUID id,
                                           @Valid @RequestBody ActualizarCosaRequest request) {
        return actualizar.ejecutar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        desactivar.ejecutar(id);
    }
}
