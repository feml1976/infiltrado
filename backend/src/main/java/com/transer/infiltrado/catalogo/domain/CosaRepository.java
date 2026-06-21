package com.transer.infiltrado.catalogo.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CosaRepository {

    Cosa guardar(Cosa cosa);

    Optional<Cosa> buscarPorId(UUID id);

    /** Lista cosas activas sin imagen_base64 (proyección ligera para HTTP listings). */
    List<Cosa> listarActivos();

    boolean existePorNombre(String nombre);

    boolean existePorNombreExcluyendo(String nombre, UUID idExcluido);

    void desactivar(UUID id, Instant eliminadoEn);

    /** Selección aleatoria con imagen incluida — uso interno desde módulo partida. */
    Optional<Cosa> seleccionarAleatoria();
}
