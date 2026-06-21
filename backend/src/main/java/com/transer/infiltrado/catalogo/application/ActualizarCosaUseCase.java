package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.application.dto.ActualizarCosaRequest;
import com.transer.infiltrado.catalogo.application.dto.CosaDetalleResponse;
import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.catalogo.domain.TipoCosa;
import com.transer.infiltrado.catalogo.domain.exception.CosaNoEncontradaException;
import com.transer.infiltrado.catalogo.domain.exception.NombreCosaDuplicadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActualizarCosaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActualizarCosaUseCase.class);

    private final CosaRepository cosaRepository;
    private final ValidadorImagen validadorImagen;

    public ActualizarCosaUseCase(CosaRepository cosaRepository, ValidadorImagen validadorImagen) {
        this.cosaRepository  = cosaRepository;
        this.validadorImagen = validadorImagen;
    }

    @Transactional
    public CosaDetalleResponse ejecutar(UUID id, ActualizarCosaRequest request) {
        Cosa existente = cosaRepository.buscarPorId(id)
                .orElseThrow(CosaNoEncontradaException::new);

        String nombre = request.nombre().trim().toLowerCase();
        if (cosaRepository.existePorNombreExcluyendo(nombre, id)) {
            throw new NombreCosaDuplicadoException(nombre);
        }

        String imagenBase64;
        if (request.tipo() == TipoCosa.IMAGEN) {
            if (request.imagenBase64() != null && !request.imagenBase64().isBlank()) {
                validadorImagen.decodificarYValidar(request.imagenBase64());
                String raw = request.imagenBase64();
                imagenBase64 = raw.contains(",")
                        ? raw.substring(raw.indexOf(',') + 1).trim()
                        : raw.trim();
            } else {
                imagenBase64 = existente.getImagenBase64();
            }
        } else {
            imagenBase64 = null;
        }

        Cosa guardada = cosaRepository.guardar(
                Cosa.reconstituir(id, nombre, request.tipo(), imagenBase64,
                        existente.isActivo(), existente.getCreadoEn()));

        log.info("Cosa actualizada id={}", id);
        return CrearCosaUseCase.toDetalle(guardada);
    }
}
