package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.application.dto.CosaDetalleResponse;
import com.transer.infiltrado.catalogo.application.dto.CrearCosaRequest;
import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.catalogo.domain.TipoCosa;
import com.transer.infiltrado.catalogo.domain.exception.NombreCosaDuplicadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearCosaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearCosaUseCase.class);

    private final CosaRepository cosaRepository;
    private final ValidadorImagen validadorImagen;

    public CrearCosaUseCase(CosaRepository cosaRepository, ValidadorImagen validadorImagen) {
        this.cosaRepository  = cosaRepository;
        this.validadorImagen = validadorImagen;
    }

    @Transactional
    public CosaDetalleResponse ejecutar(CrearCosaRequest request) {
        String nombre = request.nombre().trim().toLowerCase();

        if (cosaRepository.existePorNombre(nombre)) {
            throw new NombreCosaDuplicadoException(nombre);
        }

        String imagenBase64 = null;
        if (request.tipo() == TipoCosa.IMAGEN) {
            validadorImagen.decodificarYValidar(request.imagenBase64());
            String raw = request.imagenBase64();
            // Almacenar solo el dato puro, sin prefijo data URI
            imagenBase64 = raw.contains(",")
                    ? raw.substring(raw.indexOf(',') + 1).trim()
                    : raw.trim();
        }

        Cosa guardada = cosaRepository.guardar(Cosa.nueva(nombre, request.tipo(), imagenBase64));
        log.info("Cosa creada id={} nombre={} tipo={}", guardada.getId(), guardada.getNombre(), guardada.getTipo());
        return toDetalle(guardada);
    }

    static CosaDetalleResponse toDetalle(Cosa c) {
        return new CosaDetalleResponse(c.getId(), c.getNombre(), c.getTipo(),
                c.isActivo(), c.getCreadoEn(), c.getImagenBase64());
    }
}
