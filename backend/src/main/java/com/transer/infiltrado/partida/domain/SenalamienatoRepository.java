package com.transer.infiltrado.partida.domain;

import java.util.List;
import java.util.UUID;

public interface SenalamienatoRepository {
    Senalamiento guardar(Senalamiento senalamiento);
    List<Senalamiento> buscarPorPartida(UUID idPartida);
    void eliminarPorPartida(UUID idPartida);
}
