package com.transer.infiltrado.partida.domain;

import java.util.List;
import java.util.UUID;

public interface PistaRepository {
    Pista guardar(Pista pista);
    List<Pista> buscarPorPartida(UUID idPartida);
}
