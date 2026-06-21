package com.transer.infiltrado.partida.domain;

import java.util.Optional;
import java.util.UUID;

public interface PartidaRepository {
    Partida guardar(Partida partida);
    Optional<Partida> buscarPorId(UUID id);
    Optional<Partida> buscarPorCodigo(String codigoSala);
    boolean existePorCodigo(String codigoSala);
}
