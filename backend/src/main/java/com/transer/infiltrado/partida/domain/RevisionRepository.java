package com.transer.infiltrado.partida.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RevisionRepository {
    Revision guardar(Revision revision);
    Optional<Revision> buscarPorId(UUID id);
    List<Revision> buscarPorPartida(UUID idPartida);
    void eliminarPorPartida(UUID idPartida);
}
